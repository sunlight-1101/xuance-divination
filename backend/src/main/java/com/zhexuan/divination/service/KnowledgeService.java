package com.zhexuan.divination.service;

import com.zhexuan.divination.entity.KnowledgeRule;
import java.util.List;

public interface KnowledgeService {
    List<KnowledgeRule> list(String type, String category, String keyword);
    KnowledgeRule saveRule(KnowledgeRule rule);
    KnowledgeRule updateRule(KnowledgeRule rule);
    void deleteRule(Long id);
    List<KnowledgeRule> findForAnalysis(String type, String question);
}

