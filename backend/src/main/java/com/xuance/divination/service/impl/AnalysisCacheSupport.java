package com.xuance.divination.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuance.divination.entity.DivinationRecord;
import com.xuance.divination.mapper.DivinationRecordMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AnalysisCacheSupport {
    private static final int RECENT_RECORD_LIMIT = 30;
    private static final int CACHE_HOURS = 24;

    private final DivinationRecordMapper recordMapper;
    private final ObjectMapper objectMapper;

    public AnalysisCacheSupport(DivinationRecordMapper recordMapper, ObjectMapper objectMapper) {
        this.recordMapper = recordMapper;
        this.objectMapper = objectMapper;
    }

    public DivinationRecord findRecent(String type, Long userId, String cacheKey) {
        if (!StringUtils.hasText(type) || !StringUtils.hasText(cacheKey)) {
            return null;
        }
        List<DivinationRecord> records = recordMapper.selectList(new LambdaQueryWrapper<DivinationRecord>()
                .eq(DivinationRecord::getType, type)
                .eq(userId != null, DivinationRecord::getUserId, userId)
                .ge(DivinationRecord::getCreateTime, LocalDateTime.now().minusHours(CACHE_HOURS))
                .isNotNull(DivinationRecord::getResultJson)
                .orderByDesc(DivinationRecord::getCreateTime)
                .last("LIMIT " + RECENT_RECORD_LIMIT));
        for (DivinationRecord record : records) {
            Map<String, Object> input = readInput(record.getInputJson());
            if (input == null) {
                continue;
            }
            String existingKey = string(input.get("cacheKey"));
            if (!StringUtils.hasText(existingKey)) {
                existingKey = string(input.get("_cacheKey"));
            }
            if (cacheKey.equals(existingKey)) {
                return record;
            }
        }
        return null;
    }

    public String fingerprint(String... values) {
        StringJoiner joiner = new StringJoiner("|");
        for (String value : values) {
            joiner.add(normalize(value));
        }
        return sha256(joiner.toString());
    }

    public String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.replaceAll("\\s+", " ").trim().toLowerCase();
    }

    public String string(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private Map<String, Object> readInput(String inputJson) {
        if (!StringUtils.hasText(inputJson)) {
            return null;
        }
        try {
            return objectMapper.readValue(inputJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return null;
        }
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte item : bytes) {
                builder.append(String.format("%02x", item));
            }
            return builder.toString();
        } catch (Exception e) {
            return Integer.toHexString(value.hashCode());
        }
    }
}
