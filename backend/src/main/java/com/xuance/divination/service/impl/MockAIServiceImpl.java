package com.xuance.divination.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
            return normalizeResult(responseText);
        }

        String compactPrompt = buildCompactRetryPrompt(prompt);
        Integer retryMaxTokens = Math.max(maxTokens == null ? 0 : maxTokens, 8000);
        responseText = postChatCompletions(compactPrompt, retryMaxTokens);
        if (StringUtils.hasText(responseText)) {
            return normalizeResult(responseText);
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
                        + "JSON 必须包含 coreConclusion、plainSummary、confidence、keyEvidence、detailedAnalysis、timing、suggestion、missingFields；"
                        + "coreConclusion 必须用普通人能懂的白话直接给结论，不要放专业术语；专业依据放进 keyEvidence 和 detailedAnalysis；plainSummary 用白话展开，keyEvidence 必须是数组，timing 和 missingFields 必须是数组；"
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
                + "必须包含字段：coreConclusion, plainSummary, confidence, keyEvidence, detailedAnalysis, timing, suggestion, missingFields。coreConclusion 必须是白话，不要堆专业术语。\n\n"
                + compact;
    }

    private String normalizeResult(String raw) {
        String json = extractJsonObject(raw);
        try {
            JsonNode parsed = objectMapper.readTree(json);
            if (!parsed.isObject()) {
                return fallbackFromRaw(raw);
            }
            ObjectNode root = (ObjectNode) parsed;
            normalizeText(root, "coreConclusion", "本次分析未能稳定输出核心结论，请结合盘面重新发起分析。");
            normalizeText(root, "plainSummary", root.path("coreConclusion").asText());
            normalizeText(root, "confidence", "中等");
            normalizeText(root, "suggestion", "请结合报告中的关键依据理性参考；涉及健康、法律、投资等重大事项时，以专业人士意见为准。");
            normalizeArray(root, "keyEvidence");
            normalizeArray(root, "timing");
            normalizeArray(root, "missingFields");
            normalizeObject(root, "detailedAnalysis");
            removeMarkdownNoise(root);
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            return fallbackFromRaw(raw);
        }
    }

    private String extractJsonObject(String raw) {
        if (!StringUtils.hasText(raw)) {
            return "{}";
        }
        String text = raw.trim();
        if (text.startsWith("```")) {
            text = text.replaceFirst("^```[a-zA-Z]*\\s*", "");
            text = text.replaceFirst("\\s*```$", "");
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    private void normalizeText(ObjectNode root, String field, String fallback) {
        JsonNode value = root.get(field);
        if (value == null || value.isNull() || !StringUtils.hasText(value.asText())) {
            root.put(field, fallback);
            return;
        }
        if (!value.isTextual()) {
            root.put(field, value.toString());
        }
    }

    private void normalizeArray(ObjectNode root, String field) {
        JsonNode value = root.get(field);
        if (value == null || value.isNull()) {
            root.set(field, objectMapper.createArrayNode());
            return;
        }
        if (value.isArray()) {
            return;
        }
        ArrayNode array = objectMapper.createArrayNode();
        if (value.isObject()) {
            array.add(value.toString());
        } else if (StringUtils.hasText(value.asText())) {
            array.add(value.asText());
        }
        root.set(field, array);
    }

    private void normalizeObject(ObjectNode root, String field) {
        JsonNode value = root.get(field);
        if (value == null || value.isNull()) {
            root.set(field, objectMapper.createObjectNode());
            return;
        }
        if (value.isObject()) {
            return;
        }
        ObjectNode object = objectMapper.createObjectNode();
        if (value.isArray()) {
            object.put("summary", value.toString());
        } else if (StringUtils.hasText(value.asText())) {
            object.put("summary", value.asText());
        }
        root.set(field, object);
    }

    private void removeMarkdownNoise(ObjectNode root) {
        for (String field : new String[]{"coreConclusion", "plainSummary", "suggestion"}) {
            JsonNode value = root.get(field);
            if (value != null && value.isTextual()) {
                root.put(field, value.asText()
                        .replace("```json", "")
                        .replace("```", "")
                        .trim());
            }
        }
    }

    private String fallbackFromRaw(String raw) {
        ObjectNode root = objectMapper.createObjectNode();
        String text = StringUtils.hasText(raw) ? raw.trim() : "AI 返回内容为空。";
        root.put("coreConclusion", compactText(text, 500));
        root.put("plainSummary", compactText(text, 900));
        root.put("confidence", "低");
        ArrayNode evidence = objectMapper.createArrayNode();
        evidence.add("AI 返回内容不是标准 JSON，系统已自动整理为可展示报告。");
        root.set("keyEvidence", evidence);
        ObjectNode detail = objectMapper.createObjectNode();
        detail.put("summary", compactText(text, 900));
        root.set("detailedAnalysis", detail);
        root.set("timing", objectMapper.createArrayNode());
        root.put("suggestion", "本次结果经过兜底整理，建议重新分析一次以获得更稳定的结构化报告。");
        root.set("missingFields", objectMapper.createArrayNode());
        try {
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            return mockResultWithError("AI 返回格式异常");
        }
    }

    private String compactText(String value, int maxLength) {
        String text = value == null ? "" : value.replaceAll("\\s+", " ").trim();
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
    }

    private Map<String, String> jsonObjectResponseFormat() {
        Map<String, String> format = new HashMap<>();
        format.put("type", "json_object");
        return format;
    }

    private String mockResultWithError(String message) {
        return "{"
                + "\"coreConclusion\":\"AI 调用失败，当前返回本地兜底结果。\","
                + "\"plainSummary\":\"AI 接口当前没有返回可用分析，系统已给出兜底提示。请检查模型、接口地址、密钥和输出额度后重试。\","
                + "\"confidence\":\"低\","
                + "\"keyEvidence\":[\"真实 AI 接口调用未成功\",\"错误信息已写入建议字段\"],"
                + "\"detailedAnalysis\":{"
                + "\"basicChart\":\"未进行真实模型分析。\","
                + "\"dayMasterStrength\":\"未进行真实模型分析。\","
                + "\"tenGods\":\"未进行真实模型分析。\","
                + "\"usefulGod\":\"未进行真实模型分析。\","
                + "\"luckYear\":\"未进行真实模型分析。请配置真实 AI 后重新分析以获取大运流年详细内容。\","
                + "\"questionFocus\":\"未进行真实模型分析。\","
                + "\"caseReference\":\"未进行真实模型分析。\""
                + "},"
                + "\"timing\":[],"
                + "\"suggestion\":\"请检查 ai.enabled、ai.base-url、ai.api-key、ai.model 配置。错误：" + escapeJson(message) + "\","
                + "\"missingFields\":[]"
                + "}";
    }

    private String mockResult() {
        return "{"
                + "\"coreConclusion\":\"当前为本地模拟分析结果，配置真实大模型后会基于盘面和知识库生成判断。\","
                + "\"plainSummary\":\"当前后端未启用真实 AI，返回的是本地模拟报告。正式使用时，系统会结合问题、盘面、知识库规则和典籍片段输出结构化分析。\","
                + "\"confidence\":\"中等\","
                + "\"keyEvidence\":[\"已接收排盘结果\",\"已预留知识库规则检索\",\"报告结构已固定\"],"
                + "\"detailedAnalysis\":{"
                + "\"basicChart\":\"请填写完整出生信息后自动排盘。\","
                + "\"dayMasterStrength\":\"请填写完整出生信息后自动判断日主强弱。\","
                + "\"tenGods\":\"请填写完整出生信息后自动分析十神关系。\","
                + "\"usefulGod\":\"请根据问题类型选择用神，后续由大模型结合知识库细化。\","
                + "\"luckYear\":\"大运流转：起运时间、当前大运、未来大运趋势；流年提示：当前流年干支、十神、与大运关系、应期判断。请配置真实 AI 后重新分析获取详细内容。\","
                + "\"questionFocus\":\"请填写具体问题后自动分析重点。\","
                + "\"caseReference\":\"请配置知识库后自动检索相关案例。\""
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
