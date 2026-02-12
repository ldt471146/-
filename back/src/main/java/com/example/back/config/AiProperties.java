package com.example.back.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
    /**
     * 备用模型列表（主模型不可用时依次尝试）
     */
    private List<String> fallbackModels = new ArrayList<>();
}
