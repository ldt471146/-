package com.example.back.vo;

import lombok.Data;

/**
 * 教师端题目选项
 */
@Data
public class TeacherQuestionOptionVO {
    private String label;
    private String content;
    private Integer isCorrect;
}
