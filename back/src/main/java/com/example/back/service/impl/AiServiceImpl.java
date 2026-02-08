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
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 助手服务实现
 */
@Slf4j
@Service
public class AiServiceImpl implements AiService {

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
        String url = aiProperties.getBaseUrl() + "/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(aiProperties.getApiKey());

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of(
                "role", "system",
                "content", "你是青少年编程平台的友好助手，回答要简洁、清晰、有步骤。"
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

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", aiProperties.getModel());
        payload.put("messages", messages);
        payload.put("temperature", 0.5);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        String resp = restTemplate.postForObject(url, entity, String.class);
        String content = parseContent(resp);

        AiChatResponseVO vo = new AiChatResponseVO();
        vo.setContent(content);
        return vo;
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
