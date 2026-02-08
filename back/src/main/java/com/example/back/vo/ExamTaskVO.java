package com.example.back.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 考试任务视图
 */
@Data
public class ExamTaskVO {
    private Long id;
    private String title;
    private Long courseId;
    private String courseTitle;
    private Long chapterId;
    private String chapterTitle;
    private Integer questionCount;
    private Integer durationMinutes;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private Boolean submitted;
    private Integer latestScore;
}

