package com.xuance.divination.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuance.divination.entity.ClassicBook;
import com.xuance.divination.entity.ClassicChapter;
import com.xuance.divination.mapper.ClassicBookMapper;
import com.xuance.divination.mapper.ClassicChapterMapper;
import com.xuance.divination.service.ClassicBookService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ClassicBookServiceImpl implements ClassicBookService {
    private final ClassicBookMapper bookMapper;
    private final ClassicChapterMapper chapterMapper;
    private static final Set<String> STOP_TOKENS = Set.of(
            "八字", "六爻", "紫微", "斗数", "命盘", "命理", "分析", "综合", "判断", "问题", "断事",
            "渊海子平", "子平真诠", "滴天髓", "滴天髓阐微", "穷通宝鉴", "三命通会", "神峰通考",
            "千里命稿", "命理探原", "卜筮正宗", "增删卜易", "卜筮全书", "断易天机", "黄金策");

    public ClassicBookServiceImpl(ClassicBookMapper bookMapper, ClassicChapterMapper chapterMapper) {
        this.bookMapper = bookMapper;
        this.chapterMapper = chapterMapper;
    }

    @Override
    public List<ClassicBook> listBooks() {
        return bookMapper.selectList(new LambdaQueryWrapper<ClassicBook>()
                .eq(ClassicBook::getEnabled, 1)
                .orderByAsc(ClassicBook::getSortOrder)
                .orderByAsc(ClassicBook::getId));
    }

    @Override
    public List<ClassicChapter> listChapters(Long bookId, String keyword) {
        LambdaQueryWrapper<ClassicChapter> query = new LambdaQueryWrapper<ClassicChapter>()
                .eq(bookId != null, ClassicChapter::getBookId, bookId)
                .and(StringUtils.hasText(keyword), q -> q
                        .like(ClassicChapter::getTitle, keyword)
                        .or()
                        .like(ClassicChapter::getOriginalText, keyword)
                        .or()
                        .like(ClassicChapter::getPlainText, keyword)
                        .or()
                        .like(ClassicChapter::getNotes, keyword))
                .orderByAsc(ClassicChapter::getChapterOrder)
                .orderByAsc(ClassicChapter::getId);
        return chapterMapper.selectList(query);
    }

    @Override
    public List<String> findReferenceSnippets(String type, String context, int limit) {
        if (!StringUtils.hasText(context) || limit <= 0) {
            return new ArrayList<>();
        }
        List<ClassicBook> books = listBooks().stream()
                .filter(book -> acceptsType(type, book))
                .collect(Collectors.toList());
        if (books.isEmpty()) {
            return new ArrayList<>();
        }
        List<ReferenceCandidate> candidates = new ArrayList<>();
        for (ClassicBook book : books) {
            List<ClassicChapter> chapters = chapterMapper.selectList(new LambdaQueryWrapper<ClassicChapter>()
                    .eq(ClassicChapter::getBookId, book.getId())
                    .eq(ClassicChapter::getContentStatus, "FULL")
                    .notLike(ClassicChapter::getNotes, "OCR\u81ea\u52a8\u8bc6\u522b")
                    .orderByAsc(ClassicChapter::getChapterOrder)
                    .last("LIMIT 120"));
            for (ClassicChapter chapter : chapters) {
                if (!isUsefulReference(chapter)) {
                    continue;
                }
                int score = scoreChapter(chapter, context);
                if (score > 0) {
                    candidates.add(new ReferenceCandidate(book, chapter, score));
                }
            }
        }
        return candidates.stream()
                .sorted(Comparator.comparingInt((ReferenceCandidate item) -> item.score).reversed())
                .limit(limit)
                .map(this::formatReference)
                .collect(Collectors.toList());
    }

    private boolean acceptsType(String type, ClassicBook book) {
        String name = safe(book.getName());
        String focus = safe(book.getFocus());
        if ("BAZI".equalsIgnoreCase(type)) {
            return containsAny(name + focus, Arrays.asList(
                    "\u6ef4\u5929\u9ad3",
                    "\u6ef4\u5929\u9ad3\u9610\u5fae",
                    "\u7a77\u901a\u5b9d\u9274",
                    "\u4e09\u547d\u901a\u4f1a",
                    "\u6e0a\u6d77\u5b50\u5e73",
                    "\u5b50\u5e73\u771f\u8be0",
                    "\u795e\u5cf0\u901a\u8003",
                    "\u5343\u91cc\u547d\u7a3f",
                    "\u547d\u7406\u63a2\u539f",
                    "\u516b\u5b57\u63d0\u8981",
                    "\u5929\u5143\u5deb\u54b8",
                    "\u4e94\u884c\u7cbe\u7eaa",
                    "\u674e\u865a\u4e2d\u547d\u4e66"));
        }
        if ("LIUYAO".equalsIgnoreCase(type)) {
            return containsAny(name + focus, Arrays.asList(
                    "\u706b\u73e0\u6797",
                    "\u6613\u7ecf",
                    "\u535c\u7b6e\u6b63\u5b97",
                    "\u535c\u7b6e\u5168\u4e66",
                    "\u589e\u5220\u535c\u6613",
                    "\u65ad\u6613\u5929\u673a",
                    "\u9ec4\u91d1\u7b56",
                    "\u6885\u82b1\u6613\u6570",
                    "\u5468\u6613"));
        }
        if ("ZIWEI".equalsIgnoreCase(type)) {
            return containsAny(name + focus, Arrays.asList(
                    "\u7d2b\u5fae\u6597\u6570",
                    "\u7d2b\u5fae\u6597\u6570\u5168\u4e66",
                    "\u6597\u6570",
                    "\u592a\u5fae\u8d4b",
                    "\u589e\u8865\u592a\u5fae\u8d4b",
                    "\u9aa8\u9ad3\u8d4b",
                    "\u6597\u6570\u9aa8\u9ad3\u8d4b",
                    "\u5341\u55bb\u6b4c",
                    "\u5973\u547d\u9aa8\u9ad3\u8d4b",
                    "\u5b9a\u5bcc\u8d35\u8d2b\u8d31\u5341\u7b49\u8bba",
                    "\u5341\u516b\u98de\u661f"));
        }
        return false;
    }

    private int scoreChapter(ClassicChapter chapter, String context) {
        String haystack = safe(chapter.getTitle()) + " " + safe(chapter.getOriginalText()) + " " + safe(chapter.getPlainText()) + " " + safe(chapter.getNotes());
        List<String> tokens = tokens(context);
        int score = 0;
        for (String token : tokens) {
            if (token.length() >= 2 && haystack.contains(token)) {
                score += token.length() >= 4 ? 5 : 2;
            }
        }
        for (String token : tokens) {
            if (token.length() >= 2 && safe(chapter.getTitle()).contains(token)) {
                score += 8;
            }
        }
        return score;
    }

    private List<String> tokens(String context) {
        List<String> result = new ArrayList<>();
        String normalized = safe(context).replaceAll("[,，、。；;：:\\s]+", " ");
        for (String part : normalized.split(" ")) {
            String token = part.trim();
            if (token.length() >= 2 && token.length() <= 12 && !STOP_TOKENS.contains(token)) {
                result.add(token);
            }
        }
        return result.stream().distinct().collect(Collectors.toList());
    }

    private boolean isUsefulReference(ClassicChapter chapter) {
        String title = safe(chapter.getTitle());
        String notes = safe(chapter.getNotes());
        String text = safe(StringUtils.hasText(chapter.getPlainText()) ? chapter.getPlainText() : chapter.getOriginalText());
        String compact = text.replaceAll("\\s+", "");
        if (containsAny(title + notes, Arrays.asList("目录", "总目", "卷目", "索引", "提要", "导读", "校勘说明"))) {
            return false;
        }
        if (containsAny(compact.substring(0, Math.min(compact.length(), 80)), Arrays.asList("目录", "总目录", "卷目"))) {
            return false;
        }
        if (compact.length() < 40) {
            return false;
        }
        int chapterMarkerCount = countContains(compact, Arrays.asList("卷一", "卷二", "卷三", "卷四", "上卷", "下卷", "目录", "篇目"));
        return chapterMarkerCount < 3;
    }

    private int countContains(String value, List<String> words) {
        int count = 0;
        for (String word : words) {
            if (value.contains(word)) {
                count++;
            }
        }
        return count;
    }

    private boolean containsAny(String value, List<String> words) {
        for (String word : words) {
            if (value.contains(word)) {
                return true;
            }
        }
        return false;
    }

    private String formatReference(ReferenceCandidate item) {
        String text = StringUtils.hasText(item.chapter.getPlainText()) ? item.chapter.getPlainText() : item.chapter.getOriginalText();
        return "\u300a" + item.book.getName() + "\u300b" + item.chapter.getTitle() + "\uff1a" + snippet(text, 120);
    }

    private String snippet(String value, int maxLength) {
        String text = safe(value).replaceAll("\\s+", " ").trim();
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private static class ReferenceCandidate {
        private final ClassicBook book;
        private final ClassicChapter chapter;
        private final int score;

        private ReferenceCandidate(ClassicBook book, ClassicChapter chapter, int score) {
            this.book = book;
            this.chapter = chapter;
            this.score = score;
        }
    }
}
