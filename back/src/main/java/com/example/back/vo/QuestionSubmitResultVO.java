package com.example.back.vo;

import lombok.Data;

import java.util.List;

/**
 * 提交结果
 */
@Data
public class QuestionSubmitResultVO {
    private Long questionId;
    private boolean correct;
    private List<String> correctAnswers;
    private String analysis;
}
