package com.xuance.divination.controller;

import com.xuance.divination.common.Result;
import com.xuance.divination.dto.BaziAnalyzeDTO;
import com.xuance.divination.dto.BaziCompatibilityDTO;
import com.xuance.divination.service.BaziAnalyzeService;
import com.xuance.divination.service.impl.AnalysisTaskGuard;
import com.xuance.divination.vo.BaziAnalyzeVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bazi")
public class BaziController {
    private final BaziAnalyzeService service;
    private final AnalysisTaskGuard taskGuard;

    public BaziController(BaziAnalyzeService service, AnalysisTaskGuard taskGuard) {
        this.service = service;
        this.taskGuard = taskGuard;
    }

    @PostMapping("/analyze")
    public Result<BaziAnalyzeVO> analyze(@RequestBody BaziAnalyzeDTO dto) {
        taskGuard.enter(dto.getUserId());
        try {
            return Result.ok(service.analyze(dto));
        } finally {
            taskGuard.exit(dto.getUserId());
        }
    }

    @PostMapping("/compatibility")
    public Result<BaziAnalyzeVO> analyzeCompatibility(@RequestBody BaziCompatibilityDTO dto) {
        taskGuard.enter(dto.getUserId());
        try {
            return Result.ok(service.analyzeCompatibility(dto));
        } finally {
            taskGuard.exit(dto.getUserId());
        }
    }
}
