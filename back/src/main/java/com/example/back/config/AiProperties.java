package com.example.back.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {
    private String baseUrl;
    private String apiKey;
    private String model;
}
