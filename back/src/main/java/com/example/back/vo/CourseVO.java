package com.example.back.vo;

import lombok.Data;

/**
 * 课程列表返回
 */
@Data
public class CourseVO {
    private Long id;
    private String title;
    private String cover;
    private String intro;
    private String teacherName;
    private Integer finishStatus;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;

    /**
     * 总课时数
     */
    private Integer totalLessons;

    /**
     * 已完成课时数
     */
    private Integer finishedLessons;

    /**
     * 完成进度（0-100）
     */
    private Integer progress;

    /**
     * 最近学习课时ID
     */
    private Long lastLessonId;

    /**
     * 最近学习课时标题
     */
    private String lastLessonTitle;

    /**
     * 最近学习时间
     */
    private java.time.LocalDateTime lastLearnAt;
}
