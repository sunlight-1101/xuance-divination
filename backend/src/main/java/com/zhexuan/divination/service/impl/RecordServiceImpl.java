package com.zhexuan.divination.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhexuan.divination.common.BizException;
import com.zhexuan.divination.common.PageResult;
import com.zhexuan.divination.entity.DivinationRecord;
import com.zhexuan.divination.entity.KnowledgeRule;
import com.zhexuan.divination.entity.User;
import com.zhexuan.divination.mapper.DivinationRecordMapper;
import com.zhexuan.divination.mapper.KnowledgeRuleMapper;
import com.zhexuan.divination.mapper.UserMapper;
import com.zhexuan.divination.service.RecordService;
import com.zhexuan.divination.vo.RecordVO;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class RecordServiceImpl implements RecordService {
    private final DivinationRecordMapper mapper;
    private final KnowledgeRuleMapper knowledgeRuleMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    public RecordServiceImpl(DivinationRecordMapper mapper, KnowledgeRuleMapper knowledgeRuleMapper, UserMapper userMapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.knowledgeRuleMapper = knowledgeRuleMapper;
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public PageResult<RecordVO> list(Long userId, String type, String keyword, long page, long size) {
        Page<DivinationRecord> pageParam = new Page<>(page, size);
        Page<DivinationRecord> result = mapper.selectPage(pageParam, new LambdaQueryWrapper<DivinationRecord>()
                .eq(userId != null, DivinationRecord::getUserId, userId)
                .eq(StringUtils.hasText(type), DivinationRecord::getType, type)
                .like(StringUtils.hasText(keyword), DivinationRecord::getQuestion, keyword)
                .orderByDesc(DivinationRecord::getCreateTime));
        List<RecordVO> voList = result.getRecords().stream()
                .map(record -> toVO(record, false))
                .collect(Collectors.toList());
        return PageResult.of(voList, result.getTotal(), page, size);
    }

    @Override
    public PageResult<RecordVO> listAll(Long adminUserId, String type, String keyword, long page, long size) {
        if (adminUserId == null) {
            throw new BizException("请先登录");
        }
        User admin = userMapper.selectById(adminUserId);
        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            throw new BizException("无权限访问");
        }
        Page<DivinationRecord> pageParam = new Page<>(page, size);
        Page<DivinationRecord> result = mapper.selectPage(pageParam, new LambdaQueryWrapper<DivinationRecord>()
                .eq(StringUtils.hasText(type), DivinationRecord::getType, type)
                .like(StringUtils.hasText(keyword), DivinationRecord::getQuestion, keyword)
                .orderByDesc(DivinationRecord::getCreateTime));
        List<DivinationRecord> records = result.getRecords();
        if (records.isEmpty()) {
            return PageResult.of(Collections.emptyList(), result.getTotal(), page, size);
        }
        List<Long> userIds = records.stream()
                .map(DivinationRecord::getUserId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        List<RecordVO> voList = records.stream().map(record -> {
            RecordVO vo = toVO(record, false);
            User u = userMap.get(record.getUserId());
            if (u != null) {
                vo.setUserEmail(u.getEmail());
                vo.setUserNickname(u.getNickname());
            }
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(voList, result.getTotal(), page, size);
    }

    @Override
    public RecordVO get(Long id) {
        return toVO(mapper.selectById(id), true);
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    private RecordVO toVO(DivinationRecord record, boolean includeRules) {
        if (record == null) {
            return null;
        }
        RecordVO vo = new RecordVO();
        vo.setId(record.getId());
        vo.setUserId(record.getUserId());
        vo.setType(record.getType());
        vo.setQuestion(record.getQuestion());
        vo.setInputJson(record.getInputJson());
        vo.setResultJson(record.getResultJson());
        vo.setResultText(record.getResultText());
        vo.setKnowledgeRuleIds(record.getKnowledgeRuleIds());
        vo.setClassicReferences(parseClassicReferences(record.getClassicReferences()));
        vo.setStatus(resolveStatus(record));
        vo.setCreateTime(record.getCreateTime());
        vo.setUpdateTime(record.getUpdateTime());
        if (includeRules) {
            vo.setKnowledgeRules(findRules(record.getKnowledgeRuleIds()));
        }
        return vo;
    }

    private String resolveStatus(DivinationRecord record) {
        if (StringUtils.hasText(record.getStatus())) {
            return record.getStatus();
        }
        return StringUtils.hasText(record.getResultJson()) ? "DONE" : "PROCESSING";
    }

    private List<KnowledgeRule> findRules(String ids) {
        if (!StringUtils.hasText(ids)) {
            return Collections.emptyList();
        }
        List<Long> ruleIds = Arrays.stream(ids.split(","))
                .filter(StringUtils::hasText)
                .map(Long::valueOf)
                .collect(Collectors.toList());
        if (ruleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return knowledgeRuleMapper.selectBatchIds(ruleIds);
    }

    private List<String> parseClassicReferences(String value) {
        if (!StringUtils.hasText(value)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException ex) {
            return Arrays.stream(value.split("\\r?\\n"))
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());
        }
    }
}
