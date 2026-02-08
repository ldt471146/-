package com.example.back.dto;

import lombok.Data;

/**
 * 编程题提交
 */
@Data
public class CodeSubmitRequest {
    private Long problemId;
    private Integer languageId;
    private String sourceCode;
}
