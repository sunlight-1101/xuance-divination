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
        List<String> classicReferences = classicBookService.findReferenceSnippets(TYPE, referenceContext, 3);
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
        return promptTemplateService.load("liuyao-skill.md")
                + "\n\n" + promptTemplateService.load("knowledge-policy.md")
                + "\n\n[USER_INPUT]\n"
                + "question=" + safe(dto.getQuestion()) + "\n"
                + "gender=" + safe(dto.getGender()) + "\n"
                + "birthDate=" + safe(dto.getBirthDate()) + "\n"
                + "birthTime=" + safe(dto.getBirthTime()) + "\n"
                + "birthPlace=" + safe(dto.getBirthPlace()) + "\n"
                + "birthDayGanZhi=" + safe(dto.getBirthDayGanZhi()) + "\n"
                + "birthDayMaster=" + safe(dto.getBirthDayMaster()) + "\n"
                + "divinationTime=" + safe(dto.getTime()) + "\n"
                + "dayGanZhi=" + safe(dto.getDayGanZhi()) + "\n"
                + "dayStem=" + safe(dto.getDayStem()) + "\n"
                + "monthBranch=" + safe(dto.getMonthBranch()) + "\n"
                + "dayBranch=" + safe(dto.getDayBranch()) + "\n"
                + "emptyBranches=" + safe(dto.getEmptyBranches()) + "\n"
                + "mainGua=" + safe(dto.getMainGua()) + "\n"
                + "changedGua=" + safe(dto.getChangedGua()) + "\n"
                + "yaoList=\n" + yaoList + "\n\n"
                + "[KNOWLEDGE_RULES]\n" + (StringUtils.hasText(knowledgeRules) ? knowledgeRules : "none") + "\n\n"
                + "[CLASSIC_REFERENCES]\n" + (StringUtils.hasText(classicReferenceText) ? classicReferenceText : "none");
    }

    private String formatYao(LiuyaoYaoDTO yao) {
        return safe(yao.getPosition())
                + " sixGod=" + safe(yao.getSixGod())
                + " sixRelative=" + safe(yao.getSixRelative())
                + " lineType=" + safe(yao.getLineType())
                + " branch=" + safe(yao.getBranch())
                + " element=" + safe(yao.getElement())
                + " coinValue=" + (yao.getCoinValue() == null ? "not_provided" : yao.getCoinValue())
                + " shi=" + Boolean.TRUE.equals(yao.getShi())
                + " ying=" + Boolean.TRUE.equals(yao.getYing())
                + " moving=" + Boolean.TRUE.equals(yao.getMoving())
                + " changedLineType=" + safe(yao.getChangedLineType())
                + " changedRelative=" + safe(yao.getChangedRelative())
                + " changedBranch=" + safe(yao.getChangedBranch())
                + " hiddenSpirit=" + safe(yao.getHiddenSpirit());
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
