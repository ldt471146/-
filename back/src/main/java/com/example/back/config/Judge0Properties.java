package com.example.back.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Judge0 配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "judge0")
public class Judge0Properties {
    private String baseUrl;
    private String apiKey;
}
