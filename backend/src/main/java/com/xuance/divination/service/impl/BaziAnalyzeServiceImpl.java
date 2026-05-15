package com.xuance.divination.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuance.divination.common.BizException;
import com.xuance.divination.dto.BaziAnalyzeDTO;
import com.xuance.divination.entity.AiCallLog;
import com.xuance.divination.entity.DivinationRecord;
import com.xuance.divination.entity.KnowledgeRule;
import com.xuance.divination.mapper.AiCallLogMapper;
import com.xuance.divination.mapper.DivinationRecordMapper;
import com.xuance.divination.service.AIService;
import com.xuance.divination.service.BaziAnalyzeService;
import com.xuance.divination.service.ClassicBookService;
import com.xuance.divination.service.KnowledgeService;
import com.xuance.divination.service.PromptTemplateService;
import com.xuance.divination.service.QuotaService;
import com.xuance.divination.vo.BaziAnalyzeVO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class BaziAnalyzeServiceImpl implements BaziAnalyzeService {
    private static final String TYPE = "BAZI";

    private final KnowledgeService knowledgeService;
    private final ClassicBookService classicBookService;
    private final PromptTemplateService promptTemplateService;
    private final AIService aiService;
    private final QuotaService quotaService;
    private final DivinationRecordMapper recordMapper;
    private final AiCallLogMapper aiCallLogMapper;
    private final ObjectMapper objectMapper;

    @Value("${ai.model:mock-local}")
    private String modelName;

    public BaziAnalyzeServiceImpl(
            KnowledgeService knowledgeService,
            ClassicBookService classicBookService,
            PromptTemplateService promptTemplateService,
            AIService aiService,
            QuotaService quotaService,
            DivinationRecordMapper recordMapper,
            AiCallLogMapper aiCallLogMapper,
            ObjectMapper objectMapper) {
        this.knowledgeService = knowledgeService;
        this.classicBookService = classicBookService;
        this.promptTemplateService = promptTemplateService;
        this.aiService = aiService;
        this.quotaService = quotaService;
        this.recordMapper = recordMapper;
        this.aiCallLogMapper = aiCallLogMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public BaziAnalyzeVO analyze(BaziAnalyzeDTO dto) {
        validate(dto);
        quotaService.consumeForAnalysis(dto.getUserId(), TYPE);
        String referenceContext = dto.getQuestion() + " " + dto.getQuestionType() + " " + dto.getBaziDetails();
        List<KnowledgeRule> rules = knowledgeService.findForAnalysis(TYPE, referenceContext);
        List<String> classicReferences = classicBookService.findReferenceSnippets(TYPE, referenceContext, 3);
        String prompt = buildPrompt(dto, rules, classicReferences);
        String resultJson = aiService.analyze(prompt);

        DivinationRecord record = new DivinationRecord();
        record.setUserId(dto.getUserId());
        record.setType(TYPE);
        record.setQuestion(dto.getQuestion());
        record.setInputJson(toJson(dto));
        record.setResultJson(resultJson);
        record.setResultText(resultJson);
        record.setKnowledgeRuleIds(rules.stream()
                .map(KnowledgeRule::getId)
                .map(String::valueOf)
                .collect(Collectors.joining(",")));
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        recordMapper.insert(record);

        AiCallLog log = new AiCallLog();
        log.setUserId(dto.getUserId());
        log.setRecordId(record.getId());
        log.setModelName(modelName);
        log.setPrompt(prompt);
        log.setResponse(resultJson);
        log.setStatus("SUCCESS");
        log.setCreateTime(LocalDateTime.now());
        aiCallLogMapper.insert(log);

        BaziAnalyzeVO vo = new BaziAnalyzeVO();
        vo.setRecordId(record.getId());
        vo.setResultJson(resultJson);
        vo.setResultText(resultJson);
        vo.setKnowledgeRules(rules);
        vo.setClassicReferences(classicReferences);
        return vo;
    }

    private void validate(BaziAnalyzeDTO dto) {
        if (!StringUtils.hasText(dto.getQuestion())) {
            throw new BizException("Question is required");
        }
        if (!StringUtils.hasText(dto.getDayPillar()) && !StringUtils.hasText(dto.getDayMaster())) {
            throw new BizException("Day pillar or day master is required");
        }
    }

    private String buildPrompt(BaziAnalyzeDTO dto, List<KnowledgeRule> rules, List<String> classicReferences) {
        String knowledgeRules = rules.stream()
                .map(this::formatKnowledgeRule)
                .collect(Collectors.joining("\n"));
        String classicReferenceText = String.join("\n", classicReferences);
        return promptTemplateService.load("bazi-skill.md")
                + "\n\n" + promptTemplateService.load("knowledge-policy.md")
                + "\n\n[USER_INPUT]\n"
                + "gender=" + safe(dto.getGender()) + "\n"
                + "birthDate=" + safe(dto.getBirthDate()) + "\n"
                + "birthTime=" + safe(dto.getBirthTime()) + "\n"
                + "birthPlace=" + safe(dto.getBirthPlace()) + "\n"
                + "yearPillar=" + safe(dto.getYearPillar()) + "\n"
                + "monthPillar=" + safe(dto.getMonthPillar()) + "\n"
                + "dayPillar=" + safe(dto.getDayPillar()) + "\n"
                + "hourPillar=" + safe(dto.getHourPillar()) + "\n"
                + "dayMaster=" + safe(dto.getDayMaster()) + "\n"
                + "luckPillar=" + safe(dto.getLuckPillar()) + "\n"
                + "currentYearPillar=" + safe(dto.getCurrentYearPillar()) + "\n"
                + "baziDetails=" + safe(dto.getBaziDetails()) + "\n"
                + "questionType=" + safe(dto.getQuestionType()) + "\n"
                + "question=" + safe(dto.getQuestion()) + "\n\n"
                + "[KNOWLEDGE_RULES]\n" + (StringUtils.hasText(knowledgeRules) ? knowledgeRules : "none") + "\n\n"
                + "[CLASSIC_REFERENCES]\n" + (StringUtils.hasText(classicReferenceText) ? classicReferenceText : "none");
    }

    private String formatKnowledgeRule(KnowledgeRule rule) {
        return "[" + safe(rule.getCategory()) + "] " + safe(rule.getTitle()) + ": "
                + compact(rule.getRuleContent(), 220);
    }

    private String compact(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String text = value.replaceAll("\\s+", " ").trim();
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
    }

    private String safe(String value) {
        return StringUtils.hasText(value) ? value : "not_provided";
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new BizException("Failed to serialize input");
        }
    }
}
