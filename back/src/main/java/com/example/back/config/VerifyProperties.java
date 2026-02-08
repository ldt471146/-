package com.example.back.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 邮箱验证码配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "spring.web.verify")
public class VerifyProperties {

    /**
     * 邮件发送冷却时间（秒）
     */
    private Integer mailLimit = 60;

    /**
     * 验证码有效期（秒）
     */
    private Integer codeExpire = 600;
}
