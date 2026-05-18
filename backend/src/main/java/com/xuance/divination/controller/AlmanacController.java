package com.xuance.divination.controller;

import com.xuance.divination.common.Result;
import com.xuance.divination.service.AlmanacService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/almanac")
public class AlmanacController {
    private final AlmanacService almanacService;

    public AlmanacController(AlmanacService almanacService) {
        this.almanacService = almanacService;
    }

    @GetMapping("/day")
    public Result<Map<String, Object>> day(@RequestParam String date) {
        return Result.ok(almanacService.getDay(date));
    }
}
