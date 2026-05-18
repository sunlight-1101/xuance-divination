package com.xuance.divination.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuance.divination.common.BizException;
import com.xuance.divination.dto.BaziAnalyzeDTO;
import com.xuance.divination.dto.BaziCompatibilityDTO;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class BaziAnalyzeServiceImpl implements BaziAnalyzeService {
    private static final String TYPE = "BAZI";
    private static final String COMPATIBILITY_TYPE = "BAZI_COMPATIBILITY";
    private static final String REPORT_STYLE_VERSION = "plain-core-v2";

    private final KnowledgeService knowledgeService;
    private final ClassicBookService classicBookService;
    private final PromptTemplateService promptTemplateService;
    private final AIService aiService;
    private final QuotaService quotaService;
    private final DivinationRecordMapper recordMapper;
    private final AiCallLogMapper aiCallLogMapper;
    private final ObjectMapper objectMapper;
    private final AnalysisCacheSupport cacheSupport;

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
            ObjectMapper objectMapper,
            AnalysisCacheSupport cacheSupport) {
        this.knowledgeService = knowledgeService;
        this.classicBookService = classicBookService;
        this.promptTemplateService = promptTemplateService;
        this.aiService = aiService;
        this.quotaService = quotaService;
        this.recordMapper = recordMapper;
        this.aiCallLogMapper = aiCallLogMapper;
        this.objectMapper = objectMapper;
        this.cacheSupport = cacheSupport;
    }

    @Override
    public BaziAnalyzeVO analyze(BaziAnalyzeDTO dto) {
        validate(dto);
        String cacheKey = baziCacheKey(dto);
        DivinationRecord cached = cacheSupport.findRecent(TYPE, dto.getUserId(), cacheKey);
        if (cached != null) {
            return cachedVO(cached);
        }
        dto.setCacheKey(cacheKey);
        quotaService.consumeForAnalysis(dto.getUserId(), TYPE);
        String referenceContext = dto.getQuestion() + " " + dto.getQuestionType() + " " + dto.getBaziDetails();
        List<KnowledgeRule> rules = knowledgeService.findForAnalysis(TYPE, referenceContext);
        List<String> classicReferences = classicBookService.findReferenceSnippets(TYPE, referenceContext, 2);
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

    @Override
    public BaziAnalyzeVO analyzeCompatibility(BaziCompatibilityDTO dto) {
        validateCompatibility(dto);
        String cacheKey = compatibilityCacheKey(dto);
        DivinationRecord cached = cacheSupport.findRecent(COMPATIBILITY_TYPE, dto.getUserId(), cacheKey);
        if (cached != null) {
            return cachedVO(cached);
        }
        dto.setCacheKey(cacheKey);
        quotaService.consumeForAnalysis(dto.getUserId(), COMPATIBILITY_TYPE);
        String referenceContext = String.join(" ",
                safe(dto.getRelationshipType()),
                safe(dto.getQuestion()),
                "合盘 关系 感情 婚姻 日柱 日支 夫妻宫 配偶星 冲合 刑害 互补",
                safe(dto.getPersonADayPillar()),
                safe(dto.getPersonBDayPillar()),
                compact(dto.getPersonABaziDetails(), 500),
                compact(dto.getPersonBBaziDetails(), 500));
        List<KnowledgeRule> rules = knowledgeService.findForAnalysis(TYPE, referenceContext);
        List<String> classicReferences = classicBookService.findReferenceSnippets(TYPE, referenceContext, 2);
        String prompt = buildCompatibilityPrompt(dto, rules, classicReferences);
        String resultJson = aiService.analyze(prompt);

        DivinationRecord record = new DivinationRecord();
        record.setUserId(dto.getUserId());
        record.setType(COMPATIBILITY_TYPE);
        record.setQuestion("合盘：" + dto.getQuestion());
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

    private BaziAnalyzeVO cachedVO(DivinationRecord record) {
        BaziAnalyzeVO vo = new BaziAnalyzeVO();
        vo.setRecordId(record.getId());
        vo.setResultJson(record.getResultJson());
        vo.setResultText(record.getResultText());
        vo.setKnowledgeRules(Collections.emptyList());
        vo.setClassicReferences(Collections.emptyList());
        return vo;
    }

    private String baziCacheKey(BaziAnalyzeDTO dto) {
        return cacheSupport.fingerprint(
                TYPE,
                REPORT_STYLE_VERSION,
                dto.getQuestionType(),
                dto.getQuestion(),
                dto.getGender(),
                dto.getBirthDate(),
                dto.getBirthTime(),
                dto.getBirthPlace(),
                dto.getYearPillar(),
                dto.getMonthPillar(),
                dto.getDayPillar(),
                dto.getHourPillar(),
                dto.getDayMaster(),
                dto.getLuckPillar(),
                dto.getCurrentYearPillar(),
                dto.getBaziDetails());
    }

    private String compatibilityCacheKey(BaziCompatibilityDTO dto) {
        return cacheSupport.fingerprint(
                COMPATIBILITY_TYPE,
                REPORT_STYLE_VERSION,
                dto.getRelationshipType(),
                dto.getQuestion(),
                dto.getPersonAName(),
                dto.getPersonAGender(),
                dto.getPersonABirthDate(),
                dto.getPersonABirthTime(),
                dto.getPersonABirthPlace(),
                dto.getPersonAYearPillar(),
                dto.getPersonAMonthPillar(),
                dto.getPersonADayPillar(),
                dto.getPersonAHourPillar(),
                dto.getPersonADayMaster(),
                dto.getPersonABaziDetails(),
                dto.getPersonBName(),
                dto.getPersonBGender(),
                dto.getPersonBBirthDate(),
                dto.getPersonBBirthTime(),
                dto.getPersonBBirthPlace(),
                dto.getPersonBYearPillar(),
                dto.getPersonBMonthPillar(),
                dto.getPersonBDayPillar(),
                dto.getPersonBHourPillar(),
                dto.getPersonBDayMaster(),
                dto.getPersonBBaziDetails());
    }

    private void validate(BaziAnalyzeDTO dto) {
        if (!StringUtils.hasText(dto.getQuestion())) {
            throw new BizException("Question is required");
        }
        if (!StringUtils.hasText(dto.getDayPillar()) && !StringUtils.hasText(dto.getDayMaster())) {
            throw new BizException("Day pillar or day master is required");
        }
    }

    private void validateCompatibility(BaziCompatibilityDTO dto) {
        if (!StringUtils.hasText(dto.getQuestion())) {
            throw new BizException("Question is required");
        }
        if (!StringUtils.hasText(dto.getPersonADayPillar()) && !StringUtils.hasText(dto.getPersonADayMaster())) {
            throw new BizException("Person A day pillar or day master is required");
        }
        if (!StringUtils.hasText(dto.getPersonBDayPillar()) && !StringUtils.hasText(dto.getPersonBDayMaster())) {
            throw new BizException("Person B day pillar or day master is required");
        }
    }

    private String buildPrompt(BaziAnalyzeDTO dto, List<KnowledgeRule> rules, List<String> classicReferences) {
        String knowledgeRules = rules.stream()
                .map(this::formatKnowledgeRule)
                .collect(Collectors.joining("\n"));
        String classicReferenceText = String.join("\n", classicReferences);
        StringBuilder userInput = new StringBuilder();
        append(userInput, "gender", dto.getGender());
        append(userInput, "birthDate", dto.getBirthDate());
        append(userInput, "birthTime", dto.getBirthTime());
        append(userInput, "birthPlace", dto.getBirthPlace());
        append(userInput, "yearPillar", dto.getYearPillar());
        append(userInput, "monthPillar", dto.getMonthPillar());
        append(userInput, "dayPillar", dto.getDayPillar());
        append(userInput, "hourPillar", dto.getHourPillar());
        append(userInput, "dayMaster", dto.getDayMaster());
        append(userInput, "luckPillar", dto.getLuckPillar());
        append(userInput, "currentYearPillar", dto.getCurrentYearPillar());
        append(userInput, "baziDetails", compact(dto.getBaziDetails(), 1600));
        append(userInput, "questionType", dto.getQuestionType());
        append(userInput, "question", dto.getQuestion());
        return promptTemplateService.load("bazi-skill.md")
                + "\n\n" + promptTemplateService.load("knowledge-policy.md")
                + "\n\n[USER_INPUT]\n" + userInput + "\n"
                + "[KNOWLEDGE_RULES]\n" + (StringUtils.hasText(knowledgeRules) ? knowledgeRules : "none") + "\n\n"
                + "[CLASSIC_REFERENCES]\n" + (StringUtils.hasText(classicReferenceText) ? classicReferenceText : "none");
    }

    private String buildCompatibilityPrompt(BaziCompatibilityDTO dto, List<KnowledgeRule> rules, List<String> classicReferences) {
        String knowledgeRules = rules.stream()
                .map(this::formatKnowledgeRule)
                .collect(Collectors.joining("\n"));
        String classicReferenceText = String.join("\n", classicReferences);
        StringBuilder userInput = new StringBuilder();
        append(userInput, "relationshipType", dto.getRelationshipType());
        append(userInput, "question", dto.getQuestion());
        append(userInput, "personAName", dto.getPersonAName());
        append(userInput, "personAGender", dto.getPersonAGender());
        append(userInput, "personABirthDate", dto.getPersonABirthDate());
        append(userInput, "personABirthTime", dto.getPersonABirthTime());
        append(userInput, "personABirthPlace", dto.getPersonABirthPlace());
        append(userInput, "personAYearPillar", dto.getPersonAYearPillar());
        append(userInput, "personAMonthPillar", dto.getPersonAMonthPillar());
        append(userInput, "personADayPillar", dto.getPersonADayPillar());
        append(userInput, "personAHourPillar", dto.getPersonAHourPillar());
        append(userInput, "personADayMaster", dto.getPersonADayMaster());
        append(userInput, "personABaziDetails", compact(dto.getPersonABaziDetails(), 1300));
        append(userInput, "personBName", dto.getPersonBName());
        append(userInput, "personBGender", dto.getPersonBGender());
        append(userInput, "personBBirthDate", dto.getPersonBBirthDate());
        append(userInput, "personBBirthTime", dto.getPersonBBirthTime());
        append(userInput, "personBBirthPlace", dto.getPersonBBirthPlace());
        append(userInput, "personBYearPillar", dto.getPersonBYearPillar());
        append(userInput, "personBMonthPillar", dto.getPersonBMonthPillar());
        append(userInput, "personBDayPillar", dto.getPersonBDayPillar());
        append(userInput, "personBHourPillar", dto.getPersonBHourPillar());
        append(userInput, "personBDayMaster", dto.getPersonBDayMaster());
        append(userInput, "personBBaziDetails", compact(dto.getPersonBBaziDetails(), 1300));
        return promptTemplateService.load("bazi-compatibility-skill.md")
                + "\n\n" + promptTemplateService.load("knowledge-policy.md")
                + "\n\n[USER_INPUT]\n" + userInput + "\n"
                + "[KNOWLEDGE_RULES]\n" + (StringUtils.hasText(knowledgeRules) ? knowledgeRules : "none") + "\n\n"
                + "[CLASSIC_REFERENCES]\n" + (StringUtils.hasText(classicReferenceText) ? classicReferenceText : "none");
    }

    private String formatKnowledgeRule(KnowledgeRule rule) {
        return "[" + safe(rule.getCategory()) + "] " + safe(rule.getTitle()) + ": "
                + compact(rule.getRuleContent(), 140);
    }

    private void append(StringBuilder builder, String key, String value) {
        if (StringUtils.hasText(value)) {
            builder.append(key).append("=").append(value.trim()).append("\n");
        }
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
