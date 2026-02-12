package com.example.back.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 本地判题配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "local-judge")
public class LocalJudgeProperties {

    /**
     * 单次运行超时时间（毫秒）
     */
    private long timeoutMs = 3000;

    /**
     * 允许读取的最大输出长度
     */
    private int maxOutputLength = 20000;

    /**
     * Python 命令
     */
    private String pythonCommand = "python";

    /**
     * C 编译命令
     */
    private String gccCommand = "gcc";

    /**
     * C++ 编译命令
     */
    private String gppCommand = "g++";
}

