package com.xuance.divination.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuance.divination.common.BizException;
import com.xuance.divination.dto.LiuyaoAnalyzeDTO;
import com.xuance.divination.dto.LiuyaoYaoDTO;
import com.xuance.divination.entity.AiCallLog;
import com.xuance.divination.entity.DivinationRecord;
import com.xuance.divination.entity.KnowledgeRule;
import com.xuance.divination.mapper.AiCallLogMapper;
import com.xuance.divination.mapper.DivinationRecordMapper;
import com.xuance.divination.service.AIService;
import com.xuance.divination.service.ClassicBookService;
import com.xuance.divination.service.KnowledgeService;
import com.xuance.divination.service.LiuyaoAnalyzeService;
import com.xuance.divination.service.PromptTemplateService;
import com.xuance.divination.service.QuotaService;
import com.xuance.divination.vo.LiuyaoAnalyzeVO;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class LiuyaoAnalyzeServiceImpl implements LiuyaoAnalyzeService {
    private static final String TYPE = "LIUYAO";
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

    public LiuyaoAnalyzeServiceImpl(
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
    public LiuyaoAnalyzeVO analyze(LiuyaoAnalyzeDTO dto) {
        validate(dto);
        String cacheKey = liuyaoCacheKey(dto);
        DivinationRecord cached = cacheSupport.findRecent(TYPE, dto.getUserId(), cacheKey);
        if (cached != null) {
            return cachedVO(cached);
        }
        dto.setCacheKey(cacheKey);
        quotaService.consumeForAnalysis(dto.getUserId(), TYPE);
        String referenceContext = dto.getQuestion() + " " + dto.getMainGua() + " " + dto.getChangedGua()
                + " " + dto.getMonthBranch() + " " + dto.getDayBranch() + " " + dto.getEmptyBranches();
        List<KnowledgeRule> rules = knowledgeService.findForAnalysis(TYPE, referenceContext);
        List<String> classicReferences = classicBookService.findReferenceSnippets(TYPE, referenceContext, 2);
        String prompt = buildPrompt(dto, rules, classicReferences);
        String inputJson = toJson(dto);
        DivinationRecord record = createPendingRecord(dto.getUserId(), dto.getQuestion(), inputJson, rules);
        String resultJson;
        try {
            resultJson = aiService.analyze(prompt);
            completeRecord(record, resultJson);
        } catch (RuntimeException ex) {
            failRecord(record, ex);
            throw ex;
        }

        AiCallLog log = new AiCallLog();
        log.setUserId(dto.getUserId());
        log.setRecordId(record.getId());
        log.setModelName(modelName);
        log.setPrompt(prompt);
        log.setResponse(resultJson);
        log.setStatus("SUCCESS");
        log.setCreateTime(LocalDateTime.now());
        aiCallLogMapper.insert(log);

        LiuyaoAnalyzeVO vo = new LiuyaoAnalyzeVO();
        vo.setRecordId(record.getId());
        vo.setResultJson(resultJson);
        vo.setResultText(resultJson);
        vo.setKnowledgeRules(rules);
        vo.setClassicReferences(classicReferences);
        return vo;
    }

    private LiuyaoAnalyzeVO cachedVO(DivinationRecord record) {
        LiuyaoAnalyzeVO vo = new LiuyaoAnalyzeVO();
        vo.setRecordId(record.getId());
        vo.setResultJson(record.getResultJson());
        vo.setResultText(record.getResultText());
        vo.setKnowledgeRules(Collections.emptyList());
        vo.setClassicReferences(Collections.emptyList());
        return vo;
    }

    private DivinationRecord createPendingRecord(Long userId, String question, String inputJson, List<KnowledgeRule> rules) {
        DivinationRecord record = new DivinationRecord();
        record.setUserId(userId);
        record.setType(TYPE);
        record.setQuestion(question);
        record.setInputJson(inputJson);
        record.setKnowledgeRuleIds(ruleIds(rules));
        record.setStatus("PROCESSING");
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        recordMapper.insert(record);
        return record;
    }

    private void completeRecord(DivinationRecord record, String resultJson) {
        record.setResultJson(resultJson);
        record.setResultText(resultJson);
        record.setStatus("DONE");
        record.setUpdateTime(LocalDateTime.now());
        recordMapper.updateById(record);
    }

    private void failRecord(DivinationRecord record, RuntimeException ex) {
        String message = ex.getMessage() == null ? "分析失败，请稍后重试。" : ex.getMessage();
        String resultJson = "{\"coreConclusion\":\"分析失败：" + escapeJson(message) + "\",\"confidence\":\"未知\",\"keyEvidence\":[],\"detailedAnalysis\":{},\"timing\":[],\"suggestion\":\"请稍后重试，或检查输入信息后重新分析。\"}";
        record.setResultJson(resultJson);
        record.setResultText(resultJson);
        record.setStatus("FAILED");
        record.setUpdateTime(LocalDateTime.now());
        recordMapper.updateById(record);
    }

    private String ruleIds(List<KnowledgeRule> rules) {
        return rules.stream()
                .map(KnowledgeRule::getId)
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String liuyaoCacheKey(LiuyaoAnalyzeDTO dto) {
        String yaoKey = dto.getYaoList().stream().map(this::formatYao).collect(Collectors.joining(";"));
        return cacheSupport.fingerprint(
                TYPE,
                REPORT_STYLE_VERSION,
                dto.getQuestion(),
                dto.getGender(),
                dto.getBirthDate(),
                dto.getBirthTime(),
                dto.getBirthPlace(),
                dto.getBirthDayGanZhi(),
                dto.getBirthDayMaster(),
                dto.getTime(),
                dto.getDayGanZhi(),
                dto.getDayStem(),
                dto.getMonthBranch(),
                dto.getDayBranch(),
                dto.getEmptyBranches(),
                dto.getMainGua(),
                dto.getChangedGua(),
                yaoKey);
    }

    private void validate(LiuyaoAnalyzeDTO dto) {
        if (!StringUtils.hasText(dto.getQuestion())) {
            throw new BizException("Question is required");
        }
        if (dto.getYaoList() == null || dto.getYaoList().size() != 6) {
            throw new BizException("Six yao lines are required");
        }
    }

    private String buildPrompt(LiuyaoAnalyzeDTO dto, List<KnowledgeRule> rules, List<String> classicReferences) {
        String yaoList = dto.getYaoList().stream().map(this::formatYao).collect(Collectors.joining("\n"));
        String knowledgeRules = rules.stream()
                .map(this::formatKnowledgeRule)
                .collect(Collectors.joining("\n"));
        String classicReferenceText = String.join("\n", classicReferences);
        StringBuilder userInput = new StringBuilder();
        append(userInput, "question", dto.getQuestion());
        append(userInput, "gender", dto.getGender());
        append(userInput, "birthDate", dto.getBirthDate());
        append(userInput, "birthTime", dto.getBirthTime());
        append(userInput, "birthPlace", dto.getBirthPlace());
        append(userInput, "birthDayGanZhi", dto.getBirthDayGanZhi());
        append(userInput, "birthDayMaster", dto.getBirthDayMaster());
        append(userInput, "divinationTime", dto.getTime());
        append(userInput, "dayGanZhi", dto.getDayGanZhi());
        append(userInput, "dayStem", dto.getDayStem());
        append(userInput, "monthBranch", dto.getMonthBranch());
        append(userInput, "dayBranch", dto.getDayBranch());
        append(userInput, "emptyBranches", dto.getEmptyBranches());
        append(userInput, "mainGua", dto.getMainGua());
        append(userInput, "changedGua", dto.getChangedGua());
        return promptTemplateService.load("liuyao-skill.md")
                + "\n\n" + promptTemplateService.load("knowledge-policy.md")
                + "\n\n[USER_INPUT]\n" + userInput
                + "yaoList=\n" + yaoList + "\n\n"
                + "[KNOWLEDGE_RULES]\n" + (StringUtils.hasText(knowledgeRules) ? knowledgeRules : "none") + "\n\n"
                + "[CLASSIC_REFERENCES]\n" + (StringUtils.hasText(classicReferenceText) ? classicReferenceText : "none");
    }

    private String formatYao(LiuyaoYaoDTO yao) {
        StringBuilder builder = new StringBuilder(safe(yao.getPosition()));
        appendInline(builder, "神", yao.getSixGod());
        appendInline(builder, "亲", yao.getSixRelative());
        appendInline(builder, "爻", yao.getLineType());
        appendInline(builder, "支", yao.getBranch());
        appendInline(builder, "行", yao.getElement());
        if (yao.getCoinValue() != null) {
            builder.append(" 值=").append(yao.getCoinValue());
        }
        if (Boolean.TRUE.equals(yao.getShi())) builder.append(" 世");
        if (Boolean.TRUE.equals(yao.getYing())) builder.append(" 应");
        if (Boolean.TRUE.equals(yao.getMoving())) builder.append(" 动");
        appendInline(builder, "变爻", yao.getChangedLineType());
        appendInline(builder, "变亲", yao.getChangedRelative());
        appendInline(builder, "变支", yao.getChangedBranch());
        appendInline(builder, "伏", yao.getHiddenSpirit());
        return builder.toString();
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

    private void appendInline(StringBuilder builder, String key, String value) {
        if (StringUtils.hasText(value)) {
            builder.append(" ").append(key).append("=").append(value.trim());
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
