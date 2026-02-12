package com.example.back.dto;

import lombok.Data;

/**
 * 教师端编程题测试用例
 */
@Data
public class TeacherCodeTestcaseRequest {
    private String inputData;
    private String outputData;
    private Integer isSample;
}

