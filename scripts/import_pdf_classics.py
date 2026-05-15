from __future__ import annotations

import re
import os
from dataclasses import dataclass
from pathlib import Path

from pypdf import PdfReader

try:
    import fitz
    from rapidocr_onnxruntime import RapidOCR
except Exception:  # pragma: no cover - optional OCR dependency
    fitz = None
    RapidOCR = None


DOCUMENTS = Path.home() / "Documents"
OUTPUT = Path("pet-runs/classic_pdf_import.sql")
REPORT_OUTPUT = Path("pet-runs/classic_pdf_import_report.txt")
OCR_CACHE = Path("pet-runs/ocr-cache")

OCR_PAGE_RANGES = {
    # 1-based inclusive ranges. Keep these focused; the script is resumable.
    "zi-ping-zhen-quan": [(16, 30)],
    "shen-feng-tong-kao": [(20, 30)],
    "yuan-hai-zi-ping": [(20, 30)],
    "zhou-yi": [(1, 10)],
}


@dataclass
class BookSpec:
    name: str
    pattern: str
    slug: str
    focus: str
    dynasty: str = ""
    author: str = ""
    sort_order: int = 1000
    use_for: str = "REFERENCE"


BOOKS = [
    BookSpec("周易", "周易", "zhou-yi", "易学总纲", "周", "", 5, "LIUYAO"),
    BookSpec("道德经（帛书版）", "道德经", "dao-de-jing-bo-shu", "哲学参照", "先秦", "老子", 6),
    BookSpec("庄子", "庄子", "zhuang-zi", "哲学参照", "先秦", "庄周", 7),
    BookSpec("滴天髓", "滴天髓", "di-tian-sui", "八字体用清浊", "明清", "京图撰", 20, "BAZI"),
    BookSpec("穷通宝鉴", "穷通宝鉴", "qiong-tong-bao-jian", "八字调候用神", "清", "余春台辑", 30, "BAZI"),
    BookSpec("三命通会", "三命通会", "san-ming-tong-hui", "八字格局神煞", "明", "万民英", 40, "BAZI"),
    BookSpec("渊海子平", "渊海子平", "yuan-hai-zi-ping", "八字子平基础", "宋", "徐子平", 50, "BAZI"),
    BookSpec("子平真诠", "子平真诠", "zi-ping-zhen-quan", "八字格局用神", "清", "沈孝瞻", 60, "BAZI"),
    BookSpec("神峰通考", "神峰通考", "shen-feng-tong-kao", "八字病药中和", "明", "张楠", 70, "BAZI"),
    BookSpec("火珠林", "火珠林", "huo-zhu-lin-pdf", "六爻纳甲源流", "", "", 120, "LIUYAO"),
    BookSpec("增删卜易", "增删卜易", "zeng-shan-bu-yi-pdf", "六爻断法案例", "清", "野鹤老人", 130, "LIUYAO"),
    BookSpec("梅花易数", "梅花易数", "mei-hua-yi-shu-pdf", "易占象数", "宋", "邵雍", 140, "LIUYAO"),
    BookSpec("六壬神课", "六壬神课", "liu-ren-shen-ke-pdf", "六壬", "", "", 210),
    BookSpec("太乙神数预测", "太乙神数预测", "tai-yi-shen-shu-pdf", "太乙", "", "", 220),
    BookSpec("皇极经世书", "皇极经世书", "huang-ji-jing-shi-pdf", "象数易学", "宋", "邵雍", 230),
    BookSpec("七政四余", "七政四余", "qi-zheng-si-yu-pdf", "星命", "", "", 240),
    BookSpec("紫微斗数全书", "紫微斗数全书.pdf", "zi-wei-dou-shu-quan-shu-pdf", "紫微斗数", "", "", 250),
    BookSpec("紫微斗数全书四卷", "紫微斗数全书》四卷", "zi-wei-dou-shu-si-juan-pdf", "紫微斗数", "", "", 251),
    BookSpec("绘图地理五诀", "绘图地理五诀", "hui-tu-di-li-wu-jue-pdf", "风水地理", "清", "赵九峰", 310),
    BookSpec("疑龙经", "疑龙经", "yi-long-jing-pdf", "风水龙法", "唐", "杨筠松", 320),
    BookSpec("阴宅集要", "阴宅集要", "yin-zhai-ji-yao-pdf", "阴宅风水", "", "", 330),
    BookSpec("葬书", "葬书", "zang-shu-pdf", "风水葬法", "晋", "郭璞", 340),
    BookSpec("金匮要略", "金匮要略", "jin-gui-yao-lue-pdf", "中医古籍", "汉", "张仲景", 410),
]


def find_pdf(pattern: str) -> Path | None:
    matches = [path for path in DOCUMENTS.glob("*.pdf") if pattern in path.name]
    if not matches:
        return None
    return sorted(matches, key=lambda item: len(item.name))[0]


def extract_pages(path: Path) -> tuple[int, list[str], str]:
    try:
        reader = PdfReader(str(path), strict=False)
        pages = []
        for page in reader.pages:
            pages.append(clean_text(page.extract_text() or ""))
        sample = "\n".join(pages[:8])
        status = "TEXT" if usable_text(sample) else "SCAN"
        return len(reader.pages), pages, status
    except Exception as exc:
        return 0, [], f"ERROR: {type(exc).__name__}: {exc}"


def clean_text(value: str) -> str:
    text = value.replace("\x00", "")
    text = re.sub(r"[ \t]+", " ", text)
    text = re.sub(r"\n{3,}", "\n\n", text)
    return text.strip()


def usable_text(value: str) -> bool:
    text = clean_text(value)
    if len(text) < 300:
        return False
    cjk = sum(1 for char in text if "\u4e00" <= char <= "\u9fff")
    return cjk >= 120 and cjk / max(len(text), 1) >= 0.25


def sql(value: str | int | None) -> str:
    if value is None:
        return "NULL"
    if isinstance(value, int):
        return str(value)
    return "'" + value.replace("\\", "\\\\").replace("'", "''") + "'"


def chunk_page(text: str, max_len: int = 5000) -> list[str]:
    if len(text) <= max_len:
        return [text]
    chunks = []
    start = 0
    while start < len(text):
        end = min(len(text), start + max_len)
        chunks.append(text[start:end])
        start = end
    return chunks


def selected_ocr_pages(slug: str, pages: int) -> list[int]:
    if os.environ.get("OCR_ENABLED", "1").lower() not in {"1", "true", "yes"}:
        return []
    ranges = OCR_PAGE_RANGES.get(slug, [])
    result = []
    for start, end in ranges:
        for page in range(max(1, start), min(pages, end) + 1):
            result.append(page)
    return sorted(set(result))


def ocr_page(ocr, doc, spec: BookSpec, page_number: int) -> str:
    cache_file = OCR_CACHE / spec.slug / f"page-{page_number:04d}.txt"
    if cache_file.exists():
        return cache_file.read_text(encoding="utf-8")
    page = doc.load_page(page_number - 1)
    pix = page.get_pixmap(matrix=fitz.Matrix(0.7, 0.7), alpha=False)
    result, _ = ocr(pix.tobytes("png"))
    lines = [item[1].strip() for item in result or [] if len(item) > 1 and item[1].strip()]
    text = clean_text("\n".join(lines))
    cache_file.parent.mkdir(parents=True, exist_ok=True)
    cache_file.write_text(text, encoding="utf-8")
    return text


def ocr_selected_pages(spec: BookSpec, path: Path, pages: int, report: list[str]) -> list[tuple[int, str]]:
    page_numbers = selected_ocr_pages(spec.slug, pages)
    if not page_numbers:
        return []
    if fitz is None or RapidOCR is None:
        report.append(f"{spec.name}: OCR skipped, missing PyMuPDF or rapidocr_onnxruntime")
        return []
    OCR_CACHE.mkdir(parents=True, exist_ok=True)
    ocr = RapidOCR()
    doc = fitz.open(str(path))
    extracted = []
    for index, page_number in enumerate(page_numbers, start=1):
        text = ocr_page(ocr, doc, spec, page_number)
        if len(text) >= 40:
            extracted.append((page_number, text))
        print(f"OCR {spec.name} page {page_number}/{pages} ({index}/{len(page_numbers)}) chars={len(text)}", flush=True)
    doc.close()
    return extracted


def book_sql(spec: BookSpec, path: Path | None, pages: int, status: str) -> list[str]:
    source = str(path) if path else ""
    description = f"由本地 PDF 导入。用途：{spec.focus}。原始页数：{pages or '未知'}。"
    source_note = "可抽取文字，已按页切入典籍库。" if status == "TEXT" else "扫描版或提取失败，已建目录；后续需要 OCR 后再导入全文。"
    if status.startswith("ERROR"):
        source_note = status[:450]
    return [
        f"DELETE cc FROM classic_chapter cc JOIN classic_book cb ON cc.book_id = cb.id WHERE cb.slug = {sql(spec.slug)};",
        f"DELETE FROM classic_book WHERE slug = {sql(spec.slug)};",
        "INSERT INTO classic_book (name, slug, author, dynasty, focus, description, source_url, source_note, copyright_status, sort_order, enabled, create_time, update_time) VALUES "
        f"({sql(spec.name)}, {sql(spec.slug)}, {sql(spec.author)}, {sql(spec.dynasty)}, {sql(spec.focus)}, {sql(description)}, {sql(source)}, {sql(source_note)}, {sql('本地用户提供 PDF，仅作个人学习参考')}, {sql(spec.sort_order)}, 1, NOW(), NOW());",
        "SET @book_id = LAST_INSERT_ID();",
    ]


def chapter_sql(title: str, volume: str, order: int, text: str, status: str, notes: str = "") -> str:
    return (
        "INSERT INTO classic_chapter (book_id, title, volume, chapter_order, original_text, plain_text, notes, source_url, content_status, create_time, update_time) VALUES "
        f"(@book_id, {sql(title)}, {sql(volume)}, {sql(order)}, {sql(text)}, {sql(text)}, {sql(notes)}, NULL, {sql(status)}, NOW(), NOW());"
    )


def main() -> None:
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    lines = ["USE xuance_divination;", "SET NAMES utf8mb4;", "SET FOREIGN_KEY_CHECKS=0;"]
    report = []
    for spec in BOOKS:
        path = find_pdf(spec.pattern)
        pages, page_texts, status = extract_pages(path) if path else (0, [], "MISSING")
        lines.extend(book_sql(spec, path, pages, status))
        inserted = 0
        if status == "TEXT":
            for page_number, text in enumerate(page_texts, start=1):
                if len(text) < 80 or not usable_text(text):
                    continue
                for part_index, chunk in enumerate(chunk_page(text), start=1):
                    suffix = f"-{part_index}" if len(text) > 5000 else ""
                    lines.append(chapter_sql(f"第{page_number}页{suffix}", f"PDF第{page_number}页", page_number * 10 + part_index, chunk, "FULL"))
                    inserted += 1
        if status != "TEXT" and path and pages > 0:
            for page_number, text in ocr_selected_pages(spec, path, pages, report):
                for part_index, chunk in enumerate(chunk_page(text), start=1):
                    suffix = f"-{part_index}" if len(text) > 5000 else ""
                    lines.append(chapter_sql(f"OCR第{page_number}页{suffix}", f"OCR第{page_number}页", page_number * 10 + part_index, chunk, "OCR_PENDING", "OCR自动识别文本，可能存在错字，适合作检索参考；校对前不参与自动分析引用。"))
                    inserted += 1
        if inserted == 0:
            note = "未提取到可检索文字，建议后续 OCR。" if status != "MISSING" else "未找到 PDF 文件。"
            lines.append(chapter_sql("导入说明", "OCR待处理", 1, note, "OUTLINE", status))
        report.append(f"{spec.name}: {status}, pages={pages}, chapters={inserted}")
    lines.append("SET FOREIGN_KEY_CHECKS=1;")
    OUTPUT.write_text("\n".join(lines) + "\n", encoding="utf-8")
    REPORT_OUTPUT.write_text("\n".join(report) + "\n", encoding="utf-8")
    print(OUTPUT.resolve())
    print(REPORT_OUTPUT.resolve())


if __name__ == "__main__":
    main()
