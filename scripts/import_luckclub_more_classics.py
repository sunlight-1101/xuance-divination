from __future__ import annotations

import re
import unicodedata
from dataclasses import dataclass
from pathlib import Path

from pypdf import PdfReader


DOWNLOADS = Path.home() / "Downloads"
OUTPUT = Path("pet-runs/luckclub_more_classic_import.sql")
REPORT = Path("pet-runs/luckclub_more_classic_import_report.txt")


@dataclass
class BookSpec:
    name: str
    filename_key: str
    slug: str
    focus: str
    dynasty: str = ""
    author: str = ""
    sort_order: int = 1000


BOOKS = [
    BookSpec("易经", "易经", "zhou-yi", "六爻易学总纲", "周", "", 5),
    BookSpec("八字提要", "八字提要", "ba-zi-ti-yao", "八字分析提纲与断法清单", "近现代", "", 40),
    BookSpec("五行精纪", "五行精纪", "wu-xing-jing-ji", "五行纳音与神煞古法", "宋", "廖中", 100),
    BookSpec("李虚中命书", "李虚中命书", "li-xu-zhong-ming-shu", "年命纳音与古法合参", "唐/后世辑本", "李虚中", 110),
]


def find_pdf(key: str) -> Path | None:
    matches = [path for path in DOWNLOADS.glob("*luckclub.pdf") if key in path.name]
    return sorted(matches, key=lambda item: len(item.name))[0] if matches else None


def clean_text(value: str) -> str:
    text = unicodedata.normalize("NFKC", value or "")
    text = text.replace("\x00", "")
    text = re.sub(r"www\.luckclub\.cn\s*·\s*古籍典藏\s*·\s*内容仅供文化学习研究", "", text)
    text = re.sub(r"www\.luckclub\.cn", "", text)
    text = re.sub(r"《》\s*", "", text)
    text = re.sub(r"》-\s*古籍典藏\s*", "", text)
    text = re.sub(r"古籍典藏\s*·\s*原文与白话译文", "", text)
    text = re.sub(r"\|\s*第\s*\d+\s*页\s*/\s*共\s*\d+\s*页", "", text)
    text = re.sub(r"[ \t]+", " ", text)
    text = re.sub(r"\n[ \t]+", "\n", text)
    text = re.sub(r"\n{3,}", "\n\n", text)
    return text.strip()


def usable_text(text: str) -> bool:
    if len(text) < 80:
        return False
    cjk = sum(1 for char in text if "\u4e00" <= char <= "\u9fff")
    return cjk >= 50 and cjk / max(len(text), 1) >= 0.2


def sql(value: str | int | None) -> str:
    if value is None:
        return "NULL"
    if isinstance(value, int):
        return str(value)
    return "'" + value.replace("\\", "\\\\").replace("'", "''") + "'"


def chunk_text(text: str, max_len: int = 7000) -> list[str]:
    if len(text) <= max_len:
        return [text]
    chunks = []
    start = 0
    while start < len(text):
        end = min(len(text), start + max_len)
        break_at = text.rfind("\n", start, end)
        if break_at <= start + 2000:
            break_at = end
        chunks.append(text[start:break_at].strip())
        start = break_at
    return [item for item in chunks if item]


def book_header(spec: BookSpec, path: Path, pages: int) -> list[str]:
    description = f"由 luckclub 古籍典藏 PDF 导入。用途：{spec.focus}。原始页数：{pages}。"
    source_note = "PDF 内含可抽取文字，未使用 OCR；已按页切入典籍库，文本含原文与白话译文，适合检索参考。"
    return [
        f"DELETE cc FROM classic_chapter cc JOIN classic_book cb ON cc.book_id = cb.id WHERE cb.slug = {sql(spec.slug)};",
        f"DELETE FROM classic_book WHERE slug = {sql(spec.slug)};",
        "INSERT INTO classic_book (name, slug, author, dynasty, focus, description, source_url, source_note, copyright_status, sort_order, enabled, create_time, update_time) VALUES "
        f"({sql(spec.name)}, {sql(spec.slug)}, {sql(spec.author)}, {sql(spec.dynasty)}, {sql(spec.focus)}, {sql(description)}, {sql(str(path))}, {sql(source_note)}, {sql('本地用户提供 PDF，仅作个人学习参考')}, {sql(spec.sort_order)}, 1, NOW(), NOW());",
        "SET @book_id = LAST_INSERT_ID();",
    ]


def chapter_sql(title: str, volume: str, order: int, text: str, notes: str) -> str:
    return (
        "INSERT INTO classic_chapter (book_id, title, volume, chapter_order, original_text, plain_text, notes, source_url, content_status, create_time, update_time) VALUES "
        f"(@book_id, {sql(title)}, {sql(volume)}, {sql(order)}, {sql(text)}, {sql(text)}, {sql(notes)}, NULL, 'FULL', NOW(), NOW());"
    )


def main() -> None:
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    lines = ["USE xuance_divination;", "SET NAMES utf8mb4;", "SET FOREIGN_KEY_CHECKS=0;"]
    report = []
    for spec in BOOKS:
        path = find_pdf(spec.filename_key)
        if not path:
            report.append(f"{spec.name}: missing")
            continue
        reader = PdfReader(str(path), strict=False)
        lines.extend(book_header(spec, path, len(reader.pages)))
        inserted = 0
        chars = 0
        for page_number, page in enumerate(reader.pages, start=1):
            text = clean_text(page.extract_text() or "")
            if not usable_text(text):
                continue
            chars += len(text)
            for part_index, chunk in enumerate(chunk_text(text), start=1):
                suffix = f"-{part_index}" if part_index > 1 else ""
                title = f"第{page_number}页{suffix}"
                lines.append(chapter_sql(title, f"PDF第{page_number}页", page_number * 10 + part_index, chunk, "luckclub 可抽取文本；未使用 OCR。"))
                inserted += 1
        if inserted == 0:
            lines.append(chapter_sql("导入说明", "文本待处理", 1, "未提取到可检索文字。", "PDF 文本层提取失败。"))
        report.append(f"{spec.name}: pages={len(reader.pages)}, chapters={inserted}, chars={chars}, file={path.name}")
    lines.append("SET FOREIGN_KEY_CHECKS=1;")
    OUTPUT.write_text("\n".join(lines) + "\n", encoding="utf-8")
    REPORT.write_text("\n".join(report) + "\n", encoding="utf-8")
    print(OUTPUT.resolve())
    print(REPORT.resolve())


if __name__ == "__main__":
    main()
