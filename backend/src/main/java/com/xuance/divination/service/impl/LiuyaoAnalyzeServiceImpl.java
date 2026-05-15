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
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class LiuyaoAnalyzeServiceImpl implements LiuyaoAnalyzeService {
    private static final String TYPE = "LIUYAO";

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

    public LiuyaoAnalyzeServiceImpl(
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
    public LiuyaoAnalyzeVO analyze(LiuyaoAnalyzeDTO dto) {
        validate(dto);
        quotaService.consumeForAnalysis(dto.getUserId(), TYPE);
        String referenceContext = dto.getQuestion() + " " + dto.getMainGua() + " " + dto.getChangedGua()
                + " " + dto.getMonthBranch() + " " + dto.getDayBranch() + " " + dto.getEmptyBranches();
        List<KnowledgeRule> rules = knowledgeService.findForAnalysis(TYPE, referenceContext);
        List<String> classicReferences = classicBookService.findReferenceSnippets(TYPE, referenceContext, 2);
        String prompt = buildPrompt(dto, rules, classicReferences);
        String resultJson = aiService.analyze(prompt);
        String inputJson = toJson(dto);

        DivinationRecord record = new DivinationRecord();
        record.setUserId(dto.getUserId());
        record.setType(TYPE);
        record.setQuestion(dto.getQuestion());
        record.setInputJson(inputJson);
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

        LiuyaoAnalyzeVO vo = new LiuyaoAnalyzeVO();
        vo.setRecordId(record.getId());
        vo.setResultJson(resultJson);
        vo.setResultText(resultJson);
        vo.setKnowledgeRules(rules);
        vo.setClassicReferences(classicReferences);
        return vo;
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
