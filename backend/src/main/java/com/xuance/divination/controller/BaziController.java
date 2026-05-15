package com.xuance.divination.controller;

import com.xuance.divination.common.Result;
import com.xuance.divination.dto.BaziAnalyzeDTO;
import com.xuance.divination.service.BaziAnalyzeService;
import com.xuance.divination.vo.BaziAnalyzeVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bazi")
public class BaziController {
    private final BaziAnalyzeService service;

    public BaziController(BaziAnalyzeService service) {
        this.service = service;
    }

    @PostMapping("/analyze")
    public Result<BaziAnalyzeVO> analyze(@RequestBody BaziAnalyzeDTO dto) {
        return Result.ok(service.analyze(dto));
    }
}

