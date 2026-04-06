package com.example.back.service.impl;

import com.example.back.config.AiProperties;
import com.example.back.dto.AiChatRequest;
import com.example.back.service.AiService;
import com.example.back.vo.AiChatResponseVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AI 助手服务实现
 */
@Slf4j
@Service
public class AiServiceImpl implements AiService {

    private static final String CHAT_COMPLETIONS_PATH = "/chat/completions";

    private final RestTemplate restTemplate;
    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiServiceImpl(RestTemplate restTemplate, AiProperties aiProperties) {
        this.restTemplate = restTemplate;
        this.aiProperties = aiProperties;
    }

    @Override
    public AiChatResponseVO chat(AiChatRequest request) {
        if (aiProperties.getApiKey() == null || aiProperties.getApiKey().isBlank()) {
            throw new IllegalArgumentException("AI API Key 未配置");
        }
        String url = buildChatCompletionsUrl(aiProperties.getBaseUrl());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(aiProperties.getApiKey());

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of(
                "role", "system",
                "content", "你是青少年编程学习平台的教学助理。回答要专业、简洁、清晰，优先给出可执行步骤，避免过度口语化和娱乐化表达。"
        ));
        if (request.getHistory() != null) {
            for (AiChatRequest.AiMessage m : request.getHistory()) {
                if (m.getRole() != null && m.getContent() != null) {
                    messages.add(Map.of("role", m.getRole(), "content", m.getContent()));
                }
            }
        }
        if (request.getMessage() != null) {
            messages.add(Map.of("role", "user", "content", request.getMessage()));
        }

        AiChatResponseVO vo = new AiChatResponseVO();
        String content = tryChatWithFallback(url, headers, messages);
        vo.setContent(content);
        return vo;
    }

    private String tryChatWithFallback(String url, HttpHeaders headers, List<Map<String, String>> messages) {
        Set<String> models = new LinkedHashSet<>();
        if (aiProperties.getModel() != null && !aiProperties.getModel().isBlank()) {
            models.add(aiProperties.getModel().trim());
        }
        if (aiProperties.getFallbackModels() != null) {
            for (String m : aiProperties.getFallbackModels()) {
                if (m != null && !m.isBlank()) {
                    models.add(m.trim());
                }
            }
        }
        if (models.isEmpty()) {
            models.add("gpt-4o-mini");
        }

        String lastError = "";
        for (String model : models) {
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("model", model);
                payload.put("messages", messages);
                payload.put("temperature", 0.5);
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
                String resp = restTemplate.postForObject(url, entity, String.class);
                String content = parseContent(resp);
                if (content != null && !content.isBlank()) {
                    return content;
                }
            } catch (HttpStatusCodeException e) {
                String body = e.getResponseBodyAsString();
                lastError = "model=" + model + ", status=" + e.getStatusCode() + ", body=" + body;
                log.warn("AI request failed, try next model: {}", lastError);
                if (!isRetryableModelError(body) && !e.getStatusCode().is5xxServerError()) {
                    break;
                }
            } catch (Exception e) {
                lastError = "model=" + model + ", err=" + e.getMessage();
                log.warn("AI request exception, try next model: {}", lastError);
            }
        }
        log.error("All AI models failed: {}", lastError);
        return "AI 服务当前繁忙或模型通道不可用，请稍后重试。";
    }

    String buildChatCompletionsUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("AI Base URL 未配置");
        }
        String trimmed = baseUrl.trim();
        if (trimmed.endsWith(CHAT_COMPLETIONS_PATH)) {
            return trimmed;
        }
        if (trimmed.endsWith("/")) {
            return trimmed.substring(0, trimmed.length() - 1) + CHAT_COMPLETIONS_PATH;
        }
        return trimmed + CHAT_COMPLETIONS_PATH;
    }

    private boolean isRetryableModelError(String body) {
        if (body == null) {
            return false;
        }
        String text = body.toLowerCase();
        return text.contains("no available channels")
                || text.contains("无可用渠道")
                || text.contains("model")
                || text.contains("service unavailable");
    }

    private String parseContent(String resp) {
        try {
            JsonNode root = objectMapper.readTree(resp);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                return choices.get(0).path("message").path("content").asText();
            }
        } catch (Exception e) {
            log.error("AI response parse error: {}", resp, e);
        }
        return "抱歉，暂时无法获取回答。";
    }
}
