package com.xuance.divination.controller;

import com.xuance.divination.common.Result;
import com.xuance.divination.dto.ZiweiAnalyzeDTO;
import com.xuance.divination.dto.ZiweiChartDTO;
import com.xuance.divination.service.ZiweiAnalyzeService;
import com.xuance.divination.service.ZiweiChartService;
import com.xuance.divination.vo.ZiweiAnalyzeVO;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ziwei")
public class ZiweiController {
    private final ZiweiChartService chartService;
    private final ZiweiAnalyzeService analyzeService;

    public ZiweiController(ZiweiChartService chartService, ZiweiAnalyzeService analyzeService) {
        this.chartService = chartService;
        this.analyzeService = analyzeService;
    }

    @PostMapping("/chart")
    public Result<Map<String, Object>> chart(@RequestBody ZiweiChartDTO dto) {
        return Result.ok(chartService.buildChart(dto));
    }

    @PostMapping("/analyze")
    public Result<ZiweiAnalyzeVO> analyze(@RequestBody ZiweiAnalyzeDTO dto) {
        return Result.ok(analyzeService.analyze(dto));
    }
}
