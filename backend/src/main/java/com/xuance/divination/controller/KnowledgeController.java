package com.xuance.divination.controller;

import com.xuance.divination.common.Result;
import com.xuance.divination.common.BizException;
import com.xuance.divination.entity.KnowledgeRule;
import com.xuance.divination.entity.User;
import com.xuance.divination.mapper.UserMapper;
import com.xuance.divination.service.KnowledgeService;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {
    private final KnowledgeService service;
    private final UserMapper userMapper;

    public KnowledgeController(KnowledgeService service, UserMapper userMapper) {
        this.service = service;
        this.userMapper = userMapper;
    }

    @GetMapping("/list")
    public Result<List<KnowledgeRule>> list(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        return Result.ok(service.list(type, category, keyword));
    }

    @PostMapping("/create")
    public Result<KnowledgeRule> create(@RequestBody KnowledgeRule rule, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        rule.setOperatorUserId(userId);
        requireAdmin(userId);
        return Result.ok(service.saveRule(rule));
    }

    @PutMapping("/update")
    public Result<KnowledgeRule> update(@RequestBody KnowledgeRule rule, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        rule.setOperatorUserId(userId);
        requireAdmin(userId);
        return Result.ok(service.updateRule(rule));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        requireAdmin(userId);
        service.deleteRule(id);
        return Result.ok(true);
    }

    private void requireAdmin(Long userId) {
        if (userId == null) {
            throw new BizException("仅管理员可维护知识库");
        }
        User user = userMapper.selectById(userId);
        if (user == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new BizException("仅管理员可维护知识库");
        }
    }
}
