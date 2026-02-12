package com.example.back.vo;

import lombok.Data;

import java.util.List;

/**
 * 编程题提交结果
 */
@Data
public class CodeSubmitResultVO {
    private Long problemId;
    /**
     * 判题状态码：AC/WA/CE/RE/TLE/IE
     */
    private String result;
    /**
     * 判题状态文案
     */
    private String resultLabel;
    /**
     * 错误类型：NONE/WRONG_ANSWER/COMPILE_ERROR/RUNTIME_ERROR/TIMEOUT/SYSTEM_ERROR
     */
    private String errorType;
    /**
     * 首个失败用例序号（从 1 开始），无失败时为空
     */
    private Integer failedCaseIndex;
    private Integer passed;
    private Integer total;
    private List<String> messages;
}
