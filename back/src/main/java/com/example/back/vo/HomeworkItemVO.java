package com.example.back.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作业列表项
 */
@Data
public class HomeworkItemVO {

    private Long id;

    private Long courseId;

    private String courseTitle;

    private String title;

    private LocalDateTime deadline;

    private Integer questionCount;

    private Integer totalScore;

    private LocalDateTime createdAt;
}

