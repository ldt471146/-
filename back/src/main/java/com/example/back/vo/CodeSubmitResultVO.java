package com.example.back.vo;

import lombok.Data;

import java.util.List;

/**
 * 编程题提交结果
 */
@Data
public class CodeSubmitResultVO {
    private Long problemId;
    private String result;
    private Integer passed;
    private Integer total;
    private List<String> messages;
}
