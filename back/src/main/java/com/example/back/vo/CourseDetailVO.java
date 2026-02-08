package com.example.back.vo;

import lombok.Data;

import java.util.List;

/**
 * 课程详情返回
 */
@Data
public class CourseDetailVO {
    private Long id;
    private String title;
    private String cover;
    private String intro;
    private String teacherName;
    private Integer finishStatus;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
    private List<ChapterVO> chapters;
}
