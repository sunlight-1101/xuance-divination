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

import javax.servlet.http.HttpServletRequest;

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
    public Result<BaziAnalyzeVO> analyze(@RequestBody BaziAnalyzeDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        dto.setUserId(userId);
        taskGuard.enter(userId);
        try {
            return Result.ok(service.analyze(dto));
        } finally {
            taskGuard.exit(userId);
        }
    }

    @PostMapping("/compatibility")
    public Result<BaziAnalyzeVO> analyzeCompatibility(@RequestBody BaziCompatibilityDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        dto.setUserId(userId);
        taskGuard.enter(userId);
        try {
            return Result.ok(service.analyzeCompatibility(dto));
        } finally {
            taskGuard.exit(userId);
        }
    }
}
