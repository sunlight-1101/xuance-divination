package com.zhexuan.divination.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhexuan.divination.common.BizException;
import com.zhexuan.divination.dto.ZiweiAnalyzeDTO;
import com.zhexuan.divination.entity.AiCallLog;
import com.zhexuan.divination.entity.DivinationRecord;
import com.zhexuan.divination.entity.KnowledgeRule;
import com.zhexuan.divination.mapper.AiCallLogMapper;
import com.zhexuan.divination.mapper.DivinationRecordMapper;
import com.zhexuan.divination.service.AIService;
import com.zhexuan.divination.service.ClassicBookService;
import com.zhexuan.divination.service.KnowledgeService;
import com.zhexuan.divination.service.PromptTemplateService;
import com.zhexuan.divination.service.QuotaService;
import com.zhexuan.divination.service.ZiweiAnalyzeService;
import com.zhexuan.divination.vo.ZiweiAnalyzeVO;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ZiweiAnalyzeServiceImpl implements ZiweiAnalyzeService {
    private static final String TYPE = "ZIWEI";
    private static final String REPORT_STYLE_VERSION = "plain-core-v4-ziwei-cases";

    private final KnowledgeService knowledgeService;
    private final ClassicBookService classicBookService;
    private final PromptTemplateService promptTemplateService;
    private final AIService aiService;
    private final QuotaService quotaService;
    private final DivinationRecordMapper recordMapper;
    private final AiCallLogMapper aiCallLogMapper;
    private final ObjectMapper objectMapper;
    private final AnalysisCacheSupport cacheSupport;
    private final AnalysisTaskExecutor taskExecutor;

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
            ObjectMapper objectMapper,
            AnalysisCacheSupport cacheSupport,
            AnalysisTaskExecutor taskExecutor) {
        this.knowledgeService = knowledgeService;
        this.classicBookService = classicBookService;
        this.promptTemplateService = promptTemplateService;
        this.aiService = aiService;
        this.quotaService = quotaService;
        this.recordMapper = recordMapper;
        this.aiCallLogMapper = aiCallLogMapper;
        this.objectMapper = objectMapper;
        this.cacheSupport = cacheSupport;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public ZiweiAnalyzeVO analyze(ZiweiAnalyzeDTO dto) {
        validate(dto);
        String cacheKey = ziweiCacheKey(dto);
        DivinationRecord cached = cacheSupport.findRecent(TYPE, dto.getUserId(), cacheKey);
        if (cached != null) {
            return cachedVO(cached);
        }
        dto.setCacheKey(cacheKey);
        ensureNoActiveTask(dto.getUserId());
        quotaService.consumeForAnalysis(dto.getUserId(), TYPE);
        String chartSummary = summarizeChart(dto.getChartJson());
        String referenceContext = dto.getQuestion() + " " + dto.getQuestionType() + " " + chartSummary;
        List<KnowledgeRule> rules = knowledgeService.findForAnalysis(TYPE, referenceContext);
        List<String> classicReferences = classicBookService.findReferenceSnippets(TYPE, referenceContext, 2);
        String prompt = buildPrompt(dto, chartSummary, rules, classicReferences);
        DivinationRecord record = createPendingRecord(dto.getUserId(), dto.getQuestion(), toJson(dto), rules, classicReferences);
        taskExecutor.submit(() -> runAnalysisTask(dto.getUserId(), record, prompt));

        ZiweiAnalyzeVO vo = new ZiweiAnalyzeVO();
        vo.setRecordId(record.getId());
        vo.setStatus("PROCESSING");
        vo.setKnowledgeRules(rules);
        vo.setClassicReferences(classicReferences);
        return vo;
    }

    private ZiweiAnalyzeVO cachedVO(DivinationRecord record) {
        ZiweiAnalyzeVO vo = new ZiweiAnalyzeVO();
        vo.setRecordId(record.getId());
        vo.setStatus(StringUtils.hasText(record.getStatus()) ? record.getStatus() : "DONE");
        vo.setResultJson(record.getResultJson());
        vo.setResultText(record.getResultText());
        vo.setKnowledgeRules(Collections.emptyList());
        vo.setClassicReferences(parseClassicReferences(record.getClassicReferences()));
        return vo;
    }

    private void runAnalysisTask(Long userId, DivinationRecord record, String prompt) {
        String resultJson = null;
        try {
            resultJson = aiService.analyze(prompt);
            completeRecord(record, resultJson);
            insertAiLog(userId, record.getId(), prompt, resultJson, "SUCCESS");
        } catch (RuntimeException ex) {
            failRecord(record, ex);
            insertAiLog(userId, record.getId(), prompt, ex.getMessage(), "FAILED");
        }
    }

    private void insertAiLog(Long userId, Long recordId, String prompt, String response, String status) {
        AiCallLog log = new AiCallLog();
        log.setUserId(userId);
        log.setRecordId(recordId);
        log.setModelName(modelName);
        log.setPrompt(prompt);
        log.setResponse(response);
        log.setStatus(status);
        log.setCreateTime(LocalDateTime.now());
        aiCallLogMapper.insert(log);
    }

    private void ensureNoActiveTask(Long userId) {
        if (userId == null) {
            return;
        }
        Long activeCount = recordMapper.selectCount(new LambdaQueryWrapper<DivinationRecord>()
                .eq(DivinationRecord::getUserId, userId)
                .eq(DivinationRecord::getStatus, "PROCESSING")
                .ge(DivinationRecord::getCreateTime, LocalDateTime.now().minusMinutes(10)));
        if (activeCount != null && activeCount > 0) {
            throw new BizException("之前的报告正在分析中，请稍后再试；完成后可以在历史记录查看。");
        }
    }

    private DivinationRecord createPendingRecord(Long userId, String question, String inputJson, List<KnowledgeRule> rules, List<String> classicReferences) {
        DivinationRecord record = new DivinationRecord();
        record.setUserId(userId);
        record.setType(TYPE);
        record.setQuestion(question);
        record.setInputJson(inputJson);
        record.setKnowledgeRuleIds(ruleIds(rules));
        record.setClassicReferences(toJson(classicReferences));
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

    private String ziweiCacheKey(ZiweiAnalyzeDTO dto) {
        return cacheSupport.fingerprint(
                TYPE,
                REPORT_STYLE_VERSION,
                dto.getQuestionType(),
                dto.getQuestion(),
                dto.getGender(),
                dto.getBirthDate(),
                dto.getBirthTime(),
                dto.getBirthPlace(),
                dto.getChartJson());
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
            // 流年信息 - 放在前面确保不被截断
            Object flowYear = chart.get("flowYear");
            if (flowYear instanceof Map<?, ?>) {
                Map<?, ?> fy = (Map<?, ?>) flowYear;
                StringBuilder fyBuilder = new StringBuilder();
                fyBuilder.append("\n[FLOW_YEAR]\n");
                append(fyBuilder, "flowYearGanZhi", stringValue(fy.get("ganZhi")));
                append(fyBuilder, "flowYearMingPalace", stringValue(fy.get("mingPalaceName")));
                Object fyTransformations = fy.get("transformations");
                if (fyTransformations != null) {
                    fyBuilder.append("flowYearTransformations=").append(compact(fyTransformations.toString(), 120)).append("\n");
                }
                Object huaPositions = fy.get("huaPositions");
                if (huaPositions != null) {
                    fyBuilder.append("flowYearHuaPositions=").append(compact(huaPositions.toString(), 200)).append("\n");
                }
                // 流年宫位只列关键信息
                Object fyPalaces = fy.get("palaces");
                if (fyPalaces instanceof List<?>) {
                    List<?> fyp = (List<?>) fyPalaces;
                    fyBuilder.append("flowYearPalaces=");
                    for (Object value : fyp) {
                        if (value instanceof Map<?, ?>) {
                            Map<?, ?> fypalace = (Map<?, ?>) value;
                            String name = stringValue(fypalace.get("name"));
                            String base = stringValue(fypalace.get("basePalaceName"));
                            String stars = joinList(fypalace.get("mainStars"));
                            boolean isMing = Boolean.TRUE.equals(fypalace.get("isFlowYearMing"));
                            fyBuilder.append(name).append("(").append(base);
                            if (StringUtils.hasText(stars)) fyBuilder.append(":").append(stars);
                            if (isMing) fyBuilder.append(":流年命");
                            fyBuilder.append(") ");
                        }
                    }
                    fyBuilder.append("\n");
                }
                // 将流年信息放在本命盘前面
                builder.insert(0, fyBuilder.toString());
            }
            return compact(builder.toString(), 5000);
        } catch (Exception e) {
            return compact(chartJson, 5000);
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

    private List<String> parseClassicReferences(String value) {
        if (!StringUtils.hasText(value)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException ex) {
            return Collections.emptyList();
        }
    }
}
