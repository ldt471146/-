package com.example.back.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 作业详情
 */
@Data
public class HomeworkDetailVO {

    private Long id;

    private Long courseId;

    private String courseTitle;

    private String title;

    private LocalDateTime deadline;

    private Integer questionCount;

    private Integer totalScore;

    private LocalDateTime createdAt;

    private List<HomeworkProblemVO> problems;
}

