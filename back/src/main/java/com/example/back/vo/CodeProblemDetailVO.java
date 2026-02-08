package com.example.back.vo;

import lombok.Data;

import java.util.List;

/**
 * 编程题详情
 */
@Data
public class CodeProblemDetailVO {
    private Long id;
    private String title;
    private String content;
    private Integer difficulty;
    private Integer timeLimit;
    private Integer memoryLimit;
    private List<CodeSampleVO> samples;
}
