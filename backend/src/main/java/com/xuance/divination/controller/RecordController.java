package com.xuance.divination.controller;

import com.xuance.divination.common.Result;
import com.xuance.divination.service.RecordService;
import com.xuance.divination.vo.RecordVO;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/records")
public class RecordController {
    private final RecordService service;

    public RecordController(RecordService service) {
        this.service = service;
    }

    @GetMapping
    public Result<List<RecordVO>> list(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword) {
        return Result.ok(service.list(userId, type, keyword));
    }

    @GetMapping("/{id}")
    public Result<RecordVO> get(@PathVariable Long id) {
        return Result.ok(service.get(id));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.ok(true);
    }
}
