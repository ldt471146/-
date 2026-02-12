package com.example.back.vo;

import lombok.Data;

/**
 * 作业题目
 */
@Data
public class HomeworkProblemVO {

    private Long problemId;

    private String title;

    private Integer difficulty;

    private Integer score;
}

