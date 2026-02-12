package com.example.back.vo;

import lombok.Data;

/**
 * 教师端编程题测试用例
 */
@Data
public class TeacherCodeTestcaseVO {
    private Long id;
    private String inputData;
    private String outputData;
    private Integer isSample;
}

