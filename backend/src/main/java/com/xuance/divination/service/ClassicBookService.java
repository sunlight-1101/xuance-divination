package com.xuance.divination.service;

import com.xuance.divination.entity.ClassicBook;
import com.xuance.divination.entity.ClassicChapter;
import java.util.List;

public interface ClassicBookService {
    List<ClassicBook> listBooks();
    List<ClassicChapter> listChapters(Long bookId, String keyword);
    List<String> findReferenceSnippets(String type, String context, int limit);
}
