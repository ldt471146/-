package com.example.back.vo;

import lombok.Data;

/**
 * 教师端课程列表
 */
@Data
public class TeacherCourseVO {
    private Long id;
    private String title;
    private String cover;
    private String intro;
    private Integer status;
    private Integer finishStatus;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
    private Integer chapterCount;
    private Integer lessonCount;
}
