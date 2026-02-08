package com.example.back.service;

import com.example.back.config.Judge0Properties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Judge0 客户端
 */
@Slf4j
@Component
@Profile("judge0")
public class Judge0Client {

    private final RestTemplate restTemplate;
    private final Judge0Properties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Judge0Client(RestTemplate restTemplate, Judge0Properties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public JsonNode submit(String sourceCode, Integer languageId, String stdin, String expectedOutput) {
        String url = properties.getBaseUrl() + "/submissions?base64_encoded=false&wait=true";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (properties.getApiKey() != null && !properties.getApiKey().isBlank()) {
            headers.set("X-RapidAPI-Key", properties.getApiKey());
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("source_code", sourceCode);
        payload.put("language_id", languageId);
        payload.put("stdin", stdin);
        payload.put("expected_output", expectedOutput);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        String resp = restTemplate.postForObject(url, entity, String.class);
        try {
            return objectMapper.readTree(resp);
        } catch (Exception e) {
            log.error("Judge0 response parse error: {}", resp, e);
            return objectMapper.createObjectNode();
        }
    }
}
