package com.xuance.divination.controller;

import com.xuance.divination.common.Result;
import com.xuance.divination.dto.LiuyaoAnalyzeDTO;
import com.xuance.divination.service.LiuyaoAnalyzeService;
import com.xuance.divination.service.impl.AnalysisTaskGuard;
import com.xuance.divination.vo.LiuyaoAnalyzeVO;
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
