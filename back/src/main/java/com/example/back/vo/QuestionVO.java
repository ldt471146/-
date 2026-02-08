package com.example.back.vo;

import lombok.Data;

import java.util.List;

/**
 * 题目返回
 */
@Data
public class QuestionVO {
    private Long id;
    private String title;
    private String type;
    private Integer difficulty;
    private Long courseId;
    private Long chapterId;
    private List<QuestionOptionVO> options;
}
