package com.example.back.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 最近学习课时
 */
@Data
public class CourseLastLearnVO {
    private Long courseId;
    private Long lessonId;
    private String lessonTitle;
    private LocalDateTime learnedAt;
}
