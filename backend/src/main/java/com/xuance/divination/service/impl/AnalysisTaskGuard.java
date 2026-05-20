package com.xuance.divination.service.impl;

import com.xuance.divination.common.BizException;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class AnalysisTaskGuard {
    private static final long ACTIVE_WINDOW_MS = 10 * 60 * 1000L;
    private final ConcurrentHashMap<Long, Long> activeUsers = new ConcurrentHashMap<>();

    public void enter(Long userId) {
        if (userId == null) {
            return;
        }
        long now = System.currentTimeMillis();
        Long existing = activeUsers.putIfAbsent(userId, now);
        if (existing == null) {
            return;
        }
        if (now - existing > ACTIVE_WINDOW_MS && activeUsers.replace(userId, existing, now)) {
            return;
        }
        throw new BizException("之前的报告正在分析中，请稍后再试；完成后可以在历史记录查看。");
    }

    public void exit(Long userId) {
        if (userId != null) {
            activeUsers.remove(userId);
        }
    }
}
