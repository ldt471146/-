package com.example.back.vo;

import lombok.Data;

/**
 * 编程题列表
 */
@Data
public class CodeProblemVO {
    private Long id;
    private String title;
    private Integer difficulty;
    private Integer timeLimit;
    private Integer memoryLimit;
}
