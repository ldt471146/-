package com.example.back.dto;

import lombok.Data;

/**
 * 教师端题目选项
 */
@Data
public class TeacherQuestionOptionRequest {
    private String label;
    private String content;
    private Integer isCorrect;
}
