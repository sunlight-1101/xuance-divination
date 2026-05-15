package com.xuance.divination.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuance.divination.service.AIService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Service
public class MockAIServiceImpl implements AIService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    @Value("${ai.enabled:false}")
    private boolean enabled;

    @Value("${ai.base-url:}")
    private String baseUrl;

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.model:gpt-4o-mini}")
    private String model;

    @Value("${ai.temperature:0.2}")
    private Double temperature;

    @Value("${ai.max-tokens:1800}")
    private Integer maxTokens;

    public MockAIServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String analyze(String prompt) {
        if (!enabled || !StringUtils.hasText(apiKey) || !StringUtils.hasText(baseUrl)) {
            return mockResult();
        }
        try {
            return callChatCompletions(prompt);
        } catch (Exception e) {
            return mockResultWithError(e.getMessage());
        }
    }

    private String callChatCompletions(String prompt) throws Exception {
        String responseText = postChatCompletions(prompt, maxTokens);
        if (StringUtils.hasText(responseText)) {
            return responseText;
        }

        String compactPrompt = buildCompactRetryPrompt(prompt);
        Integer retryMaxTokens = Math.max(maxTokens == null ? 0 : maxTokens, 8000);
        responseText = postChatCompletions(compactPrompt, retryMaxTokens);
        if (StringUtils.hasText(responseText)) {
            return responseText;
        }

        throw new IllegalStateException("AI 返回内容为空，模型可能把输出额度耗在推理过程里；已自动重试仍无正文，请调高 ai.max-tokens 或改用 deepseek-chat。");
    }

    private String postChatCompletions(String prompt, Integer requestMaxTokens) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> system = new HashMap<>();
        system.put("role", "system");
        system.put("content",
                "你是玄策术数分析 Agent，负责六爻和八字断事。"
                        + "必须只基于用户提供的信息、盘面和知识库规则分析；"
                        + "不得编造未提供的爻位、四柱、大运、流年、宫位或用户背景；"
                        + "案例只能类比，不能替代真实盘面；"
                        + "必须只输出合法 JSON 对象，不输出 Markdown；"
                        + "禁止绝对化、恐吓式、宿命论表达；"
                        + "健康、法律、投资和重大财务问题只能给风险提示，不能替代专业意见。");
        messages.add(system);

        Map<String, String> user = new HashMap<>();
        user.put("role", "user");
        user.put("content", prompt);
        messages.add(user);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("temperature", temperature);
        body.put("max_tokens", requestMaxTokens);
        body.put("messages", messages);
        body.put("response_format", jsonObjectResponseFormat());

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, new HttpEntity<>(body, headers), String.class);
        JsonNode root = objectMapper.readTree(response.getBody());
        JsonNode content = root.path("choices").path(0).path("message").path("content");
        if (content.isTextual() && StringUtils.hasText(content.asText())) {
            return content.asText();
        }
        return "";
    }

    private String buildCompactRetryPrompt(String prompt) {
        int limit = 12000;
        String compact = prompt == null ? "" : prompt;
        if (compact.length() > limit) {
            compact = compact.substring(0, limit) + "\n【提示】原提示词较长，已截断非关键上下文；请依据已给盘面直接输出 JSON。";
        }
        return "上一次模型返回了空正文。请不要输出思考过程，不要输出 Markdown，只直接输出合法 JSON 对象。\n"
                + "必须包含字段：coreConclusion, confidence, keyEvidence, detailedAnalysis, timing, suggestion, missingFields。\n\n"
                + compact;
    }

    private Map<String, String> jsonObjectResponseFormat() {
        Map<String, String> format = new HashMap<>();
        format.put("type", "json_object");
        return format;
    }

    private String mockResultWithError(String message) {
        return "{"
                + "\"coreConclusion\":\"AI 调用失败，当前返回本地兜底结果。\","
                + "\"confidence\":\"低\","
                + "\"keyEvidence\":[\"真实 AI 接口调用未成功\",\"错误信息已写入建议字段\"],"
                + "\"detailedAnalysis\":{"
                + "\"yongShen\":\"请检查 AI 配置后重试。\","
                + "\"shiYing\":\"未进行真实模型分析。\","
                + "\"usefulGodState\":\"未进行真实模型分析。\","
                + "\"movingYao\":\"未进行真实模型分析。\","
                + "\"hiddenSpirit\":\"未进行真实模型分析。\","
                + "\"timing\":\"未进行真实模型分析。\""
                + "},"
                + "\"timing\":[],"
                + "\"suggestion\":\"请检查 ai.enabled、ai.base-url、ai.api-key、ai.model 配置。错误：" + escapeJson(message) + "\","
                + "\"missingFields\":[]"
                + "}";
    }

    private String mockResult() {
        return "{"
                + "\"coreConclusion\":\"当前为本地模拟分析结果，配置真实大模型后会基于盘面和知识库生成判断。\","
                + "\"confidence\":\"中等\","
                + "\"keyEvidence\":[\"已接收排盘结果\",\"已预留知识库规则检索\",\"报告结构已固定\"],"
                + "\"detailedAnalysis\":{"
                + "\"yongShen\":\"请根据问题类型选择用神，后续由大模型结合知识库细化。\","
                + "\"shiYing\":\"世爻代表求测人，应爻代表对方、环境或事情对应方。\","
                + "\"usefulGodState\":\"需要结合月建、日辰、空亡、动变判断旺衰。\","
                + "\"movingYao\":\"动爻代表变化点。\","
                + "\"hiddenSpirit\":\"如用神伏藏，需观察伏神与飞神关系。\","
                + "\"timing\":\"应期需从用神、动爻、变爻、冲合、填实综合判断。\""
                + "},"
                + "\"timing\":[],"
                + "\"suggestion\":\"先补充知识库和真实模型配置，再进行正式断事。\","
                + "\"missingFields\":[]"
                + "}";
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
