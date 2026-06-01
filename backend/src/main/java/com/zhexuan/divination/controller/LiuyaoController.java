package com.zhexuan.divination.controller;

import com.zhexuan.divination.common.Result;
import com.zhexuan.divination.dto.LiuyaoAnalyzeDTO;
import com.zhexuan.divination.service.LiuyaoAnalyzeService;
import com.zhexuan.divination.service.impl.AnalysisTaskGuard;
import com.zhexuan.divination.vo.LiuyaoAnalyzeVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/liuyao")
public class LiuyaoController {
    private final LiuyaoAnalyzeService service;
    private final AnalysisTaskGuard taskGuard;

    public LiuyaoController(LiuyaoAnalyzeService service, AnalysisTaskGuard taskGuard) {
        this.service = service;
        this.taskGuard = taskGuard;
    }

    @PostMapping("/analyze")
    public Result<LiuyaoAnalyzeVO> analyze(@RequestBody LiuyaoAnalyzeDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        dto.setUserId(userId);
        taskGuard.enter(userId);
        try {
            return Result.ok(service.analyze(dto));
        } finally {
            taskGuard.exit(userId);
        }
    }
}
