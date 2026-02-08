package com.example.back.vo;

import lombok.Data;

import java.util.List;

/**
 * 单题判定结果
 */
@Data
public class ExamQuestionResultVO {
    private Long questionId;
    private String title;
    private Boolean correct;
    private List<String> userAnswers;
    private List<String> correctAnswers;
    private String analysis;
}

