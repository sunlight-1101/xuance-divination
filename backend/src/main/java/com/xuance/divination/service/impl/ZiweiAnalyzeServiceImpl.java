package com.xuance.divination.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuance.divination.common.BizException;
import com.xuance.divination.dto.ZiweiAnalyzeDTO;
import com.xuance.divination.entity.AiCallLog;
import com.xuance.divination.entity.DivinationRecord;
import com.xuance.divination.entity.KnowledgeRule;
import com.xuance.divination.mapper.AiCallLogMapper;
import com.xuance.divination.mapper.DivinationRecordMapper;
import com.xuance.divination.service.AIService;
import com.xuance.divination.service.ClassicBookService;
import com.xuance.divination.service.KnowledgeService;
import com.xuance.divination.service.PromptTemplateService;
import com.xuance.divination.service.QuotaService;
import com.xuance.divination.service.ZiweiAnalyzeService;
import com.xuance.divination.vo.ZiweiAnalyzeVO;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ZiweiAnalyzeServiceImpl implements ZiweiAnalyzeService {
    private static final String TYPE = "ZIWEI";

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

    public ZiweiAnalyzeServiceImpl(
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
    public ZiweiAnalyzeVO analyze(ZiweiAnalyzeDTO dto) {
        validate(dto);
        quotaService.consumeForAnalysis(dto.getUserId(), TYPE);
        String chartSummary = summarizeChart(dto.getChartJson());
        String referenceContext = dto.getQuestion() + " " + dto.getQuestionType() + " " + chartSummary;
        List<KnowledgeRule> rules = knowledgeService.findForAnalysis(TYPE, referenceContext);
        List<String> classicReferences = classicBookService.findReferenceSnippets(TYPE, referenceContext, 2);
        String prompt = buildPrompt(dto, chartSummary, rules, classicReferences);
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

        ZiweiAnalyzeVO vo = new ZiweiAnalyzeVO();
        vo.setRecordId(record.getId());
        vo.setResultJson(resultJson);
        vo.setResultText(resultJson);
        vo.setKnowledgeRules(rules);
        vo.setClassicReferences(classicReferences);
        return vo;
    }

    private void validate(ZiweiAnalyzeDTO dto) {
        if (!StringUtils.hasText(dto.getQuestion())) {
            throw new BizException("Question is required");
        }
        if (!StringUtils.hasText(dto.getChartJson())) {
            throw new BizException("Ziwei chart is required");
        }
    }

    private String buildPrompt(ZiweiAnalyzeDTO dto, String chartSummary, List<KnowledgeRule> rules, List<String> classicReferences) {
        String knowledgeRules = rules.stream().map(this::formatKnowledgeRule).collect(Collectors.joining("\n"));
        String classicReferenceText = String.join("\n", classicReferences);
        StringBuilder userInput = new StringBuilder();
        append(userInput, "gender", dto.getGender());
        append(userInput, "birthDate", dto.getBirthDate());
        append(userInput, "birthTime", dto.getBirthTime());
        append(userInput, "birthPlace", dto.getBirthPlace());
        append(userInput, "questionType", dto.getQuestionType());
        append(userInput, "question", dto.getQuestion());
        return promptTemplateService.load("ziwei-skill.md")
                + "\n\n" + promptTemplateService.load("knowledge-policy.md")
                + "\n\n[USER_INPUT]\n" + userInput + "\n"
                + "[ZIWEI_CHART_SUMMARY]\n" + chartSummary + "\n\n"
                + "[KNOWLEDGE_RULES]\n" + (StringUtils.hasText(knowledgeRules) ? knowledgeRules : "none") + "\n\n"
                + "[CLASSIC_REFERENCES]\n" + (StringUtils.hasText(classicReferenceText) ? classicReferenceText : "none");
    }

    private String formatKnowledgeRule(KnowledgeRule rule) {
        return "[" + safe(rule.getCategory()) + "] " + safe(rule.getTitle()) + ": " + compact(rule.getRuleContent(), 140);
    }

    @SuppressWarnings("unchecked")
    private String summarizeChart(String chartJson) {
        try {
            Map<String, Object> chart = objectMapper.readValue(chartJson, Map.class);
            StringBuilder builder = new StringBuilder();
            append(builder, "yearGanZhi", stringValue(chart.get("yearGanZhi")));
            append(builder, "hourBranch", stringValue(chart.get("hourBranch")));
            append(builder, "mingGong", stringValue(chart.get("mingGong")));
            append(builder, "shenGong", stringValue(chart.get("shenGong")));
            append(builder, "lifeMaster", stringValue(chart.get("lifeMaster")));
            append(builder, "bodyMaster", stringValue(chart.get("bodyMaster")));
            append(builder, "douJun", stringValue(chart.get("douJun")));
            append(builder, "sourcePalace", stringValue(chart.get("sourcePalace")));
            append(builder, "fiveElementBureau", stringValue(chart.get("fiveElementBureau")));
            Object transformations = chart.get("transformations");
            if (transformations != null) {
                builder.append("transformations=").append(compact(transformations.toString(), 180)).append("\n");
            }
            Object palacesValue = chart.get("palaces");
            if (palacesValue instanceof List<?>) {
                List<?> palaces = (List<?>) palacesValue;
                builder.append("palaces=\n");
                for (Object value : palaces) {
                    if (value instanceof Map<?, ?>) {
                        Map<?, ?> palace = (Map<?, ?>) value;
                        builder.append(formatPalace((Map<String, Object>) palace)).append("\n");
                    }
                }
            }
            return compact(builder.toString(), 4200);
        } catch (Exception e) {
            return compact(chartJson, 4200);
        }
    }

    private String formatPalace(Map<String, Object> palace) {
        List<String> parts = new ArrayList<>();
        addPart(parts, "宫", palace.get("name"));
        addPart(parts, "干支", palace.get("ganZhi"));
        addPart(parts, "主", joinList(palace.get("mainStars")));
        addPart(parts, "吉", joinList(palace.get("luckyStars")));
        addPart(parts, "煞", joinList(palace.get("shaStars")));
        addPart(parts, "杂", compact(joinList(palace.get("otherStars")), 40));
        addPart(parts, "四化", joinList(palace.get("transformations")));
        addPart(parts, "长生", palace.get("lifeStage"));
        addPart(parts, "年神", palace.get("yearGod"));
        Object luck = palace.get("majorLuck");
        if (luck instanceof Map<?, ?>) {
            Map<?, ?> luckMap = (Map<?, ?>) luck;
            addPart(parts, "限", stringValue(luckMap.get("startAge")) + "-" + stringValue(luckMap.get("endAge")));
        }
        if (Boolean.TRUE.equals(palace.get("isMing"))) parts.add("命");
        if (Boolean.TRUE.equals(palace.get("isShen"))) parts.add("身");
        return String.join(" ", parts);
    }

    private void addPart(List<String> parts, String key, Object value) {
        String text = stringValue(value);
        if (StringUtils.hasText(text)) {
            parts.add(key + "=" + text);
        }
    }

    private String joinList(Object value) {
        if (!(value instanceof List<?>)) {
            return "";
        }
        List<?> list = (List<?>) value;
        if (list.isEmpty()) {
            return "";
        }
        return list.stream()
                .map(this::stringValue)
                .filter(StringUtils::hasText)
                .collect(Collectors.joining("、"));
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private void append(StringBuilder builder, String key, String value) {
        if (StringUtils.hasText(value)) {
            builder.append(key).append("=").append(value.trim()).append("\n");
        }
    }

    private String compact(String value, int maxLength) {
        if (!StringUtils.hasText(value)) return "";
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
