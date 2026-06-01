package com.zhexuan.divination.controller;

import com.zhexuan.divination.common.Result;
import com.zhexuan.divination.entity.ClassicBook;
import com.zhexuan.divination.entity.ClassicChapter;
import com.zhexuan.divination.service.ClassicBookService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/classics")
public class ClassicBookController {
    private final ClassicBookService service;

    public ClassicBookController(ClassicBookService service) {
        this.service = service;
    }

    @GetMapping("/books")
    public Result<List<ClassicBook>> books() {
        return Result.ok(service.listBooks());
    }

    @GetMapping("/chapters")
    public Result<List<ClassicChapter>> chapters(
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) String keyword) {
        return Result.ok(service.listChapters(bookId, keyword));
    }
}
