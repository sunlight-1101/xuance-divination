package com.zhexuan.divination.service.impl;

import com.zhexuan.divination.common.BizException;
import com.zhexuan.divination.service.PromptTemplateService;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

@Service
public class PromptTemplateServiceImpl implements PromptTemplateService {
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    @Override
    public String load(String name) {
        return cache.computeIfAbsent(name, this::readTemplate);
    }

    private String readTemplate(String name) {
        ClassPathResource resource = new ClassPathResource("prompts/" + name);
        try (InputStream inputStream = resource.getInputStream()) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            throw new BizException("Failed to read prompt template: " + name);
        }
    }
}
