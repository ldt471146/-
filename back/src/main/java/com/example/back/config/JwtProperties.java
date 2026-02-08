package com.example.back.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT 配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "spring.security.jwt")
public class JwtProperties {
    /**
     * JWT 密钥
     */
    private String key;

    /**
     * 过期时间（小时）
     */
    private Integer expire;

    /**
     * 记住我过期时间（小时）
     */
    private Integer rememberExpire;
}
