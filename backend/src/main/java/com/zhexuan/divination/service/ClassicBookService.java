package com.zhexuan.divination.service;

import com.zhexuan.divination.entity.ClassicBook;
import com.zhexuan.divination.entity.ClassicChapter;
import java.util.List;

public interface ClassicBookService {
    List<ClassicBook> listBooks();
    List<ClassicChapter> listChapters(Long bookId, String keyword);
    List<String> findReferenceSnippets(String type, String context, int limit);
}
