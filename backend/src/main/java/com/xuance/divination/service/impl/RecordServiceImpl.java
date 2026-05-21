package com.xuance.divination.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuance.divination.entity.DivinationRecord;
import com.xuance.divination.entity.KnowledgeRule;
import com.xuance.divination.mapper.DivinationRecordMapper;
import com.xuance.divination.mapper.KnowledgeRuleMapper;
import com.xuance.divination.service.RecordService;
import com.xuance.divination.vo.RecordVO;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class RecordServiceImpl implements RecordService {
    private final DivinationRecordMapper mapper;
    private final KnowledgeRuleMapper knowledgeRuleMapper;

    public RecordServiceImpl(DivinationRecordMapper mapper, KnowledgeRuleMapper knowledgeRuleMapper) {
        this.mapper = mapper;
        this.knowledgeRuleMapper = knowledgeRuleMapper;
    }

    @Override
    public List<RecordVO> list(Long userId, String type, String keyword) {
        return mapper.selectList(new LambdaQueryWrapper<DivinationRecord>()
                .eq(userId != null, DivinationRecord::getUserId, userId)
                .eq(StringUtils.hasText(type), DivinationRecord::getType, type)
                .like(StringUtils.hasText(keyword), DivinationRecord::getQuestion, keyword)
                .orderByDesc(DivinationRecord::getCreateTime))
                .stream()
                .map(record -> toVO(record, false))
                .collect(Collectors.toList());
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
}
