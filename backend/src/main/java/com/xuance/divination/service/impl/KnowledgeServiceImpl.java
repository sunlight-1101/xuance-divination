package com.xuance.divination.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuance.divination.entity.KnowledgeRule;
import com.xuance.divination.mapper.KnowledgeRuleMapper;
import com.xuance.divination.service.KnowledgeService;
import java.util.Comparator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class KnowledgeServiceImpl implements KnowledgeService {
    private static final int ANALYSIS_RULE_LIMIT = 4;
    private static final int FALLBACK_RULE_LIMIT = 2;

    private final KnowledgeRuleMapper mapper;

    public KnowledgeServiceImpl(KnowledgeRuleMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<KnowledgeRule> list(String type, String category, String keyword) {
        LambdaQueryWrapper<KnowledgeRule> query = new LambdaQueryWrapper<KnowledgeRule>()
                .eq(StringUtils.hasText(type), KnowledgeRule::getType, type)
                .eq(StringUtils.hasText(category), KnowledgeRule::getCategory, category)
                .and(StringUtils.hasText(keyword), q -> q
                        .like(KnowledgeRule::getTitle, keyword)
                        .or()
                        .like(KnowledgeRule::getKeywords, keyword)
                        .or()
                        .like(KnowledgeRule::getRuleContent, keyword))
                .orderByDesc(KnowledgeRule::getPriority)
                .orderByDesc(KnowledgeRule::getCreateTime);
        return mapper.selectList(query);
    }

    @Override
    public KnowledgeRule saveRule(KnowledgeRule rule) {
        rule.setCreateTime(LocalDateTime.now());
        rule.setUpdateTime(LocalDateTime.now());
        if (rule.getEnabled() == null) {
            rule.setEnabled(1);
        }
        if (rule.getPriority() == null) {
            rule.setPriority(0);
        }
        mapper.insert(rule);
        return rule;
    }

    @Override
    public KnowledgeRule updateRule(KnowledgeRule rule) {
        rule.setUpdateTime(LocalDateTime.now());
        mapper.updateById(rule);
        return mapper.selectById(rule.getId());
    }

    @Override
    public void deleteRule(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public List<KnowledgeRule> findForAnalysis(String type, String question) {
        List<KnowledgeRule> allRules = mapper.selectList(new LambdaQueryWrapper<KnowledgeRule>()
                .eq(KnowledgeRule::getType, type)
                .eq(KnowledgeRule::getEnabled, 1)
                .orderByDesc(KnowledgeRule::getPriority));
        List<KnowledgeRule> matched = allRules.stream()
                .filter(rule -> score(rule, question) > 0)
                .sorted(Comparator
                        .comparingInt((KnowledgeRule rule) -> score(rule, question)).reversed()
                        .thenComparing(Comparator.comparingInt((KnowledgeRule rule) -> priority(rule)).reversed()))
                .limit(ANALYSIS_RULE_LIMIT)
                .collect(Collectors.toList());
        includeBestMatchedCase(allRules, matched, question);
        if (!matched.isEmpty()) {
            return matched;
        }
        return allRules.stream().limit(FALLBACK_RULE_LIMIT).collect(Collectors.toList());
    }

    private void includeBestMatchedCase(List<KnowledgeRule> allRules, List<KnowledgeRule> matched, String question) {
        if (matched.isEmpty() || matched.stream().anyMatch(this::isCaseRule)) {
            return;
        }
        KnowledgeRule bestCase = allRules.stream()
                .filter(this::isCaseRule)
                .filter(rule -> score(rule, question) > 0)
                .sorted(Comparator
                        .comparingInt((KnowledgeRule rule) -> score(rule, question)).reversed()
                        .thenComparing(Comparator.comparingInt((KnowledgeRule rule) -> priority(rule)).reversed()))
                .findFirst()
                .orElse(null);
        if (bestCase == null) {
            return;
        }
        if (matched.size() >= ANALYSIS_RULE_LIMIT) {
            matched.remove(matched.size() - 1);
        }
        matched.add(bestCase);
    }

    private boolean isCaseRule(KnowledgeRule rule) {
        return StringUtils.hasText(rule.getCategory()) && rule.getCategory().contains("案例");
    }

    private int score(KnowledgeRule rule, String question) {
        if (!StringUtils.hasText(question)) {
            return 0;
        }
        int score = 0;
        String normalizedQuestion = question.trim();
        String category = trimTopic(rule.getCategory());
        if (StringUtils.hasText(category) && normalizedQuestion.contains(category)) {
            score += 40;
        }
        score += keywordScore(rule.getKeywords(), normalizedQuestion) * 12;
        score += keywordScore(rule.getTitle(), normalizedQuestion) * 8;
        if (StringUtils.hasText(rule.getRuleContent()) && rule.getRuleContent().contains(normalizedQuestion)) {
            score += 2;
        }
        return score;
    }

    private int keywordScore(String value, String question) {
        if (!StringUtils.hasText(value)) {
            return 0;
        }
        int score = 0;
        String[] words = value.split("[,，、\\s]+");
        for (String word : words) {
            if (StringUtils.hasText(word) && question.contains(word.trim())) {
                score++;
            }
        }
        return score;
    }

    private String trimTopic(String category) {
        if (!StringUtils.hasText(category)) {
            return "";
        }
        return category.replace("专题", "");
    }

    private int priority(KnowledgeRule rule) {
        return rule.getPriority() == null ? 0 : rule.getPriority();
    }
}
