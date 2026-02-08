package com.example.back.vo;

import lombok.Data;

import java.util.List;

/**
 * 考试题目
 */
@Data
public class ExamQuestionVO {
    private Long id;
    private String title;
    private String type;
    private Integer difficulty;
    private List<QuestionOptionVO> options;
}

