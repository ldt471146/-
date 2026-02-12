package com.example.back.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员课程审核视图
 */
@Data
public class AdminCourseVO {
    private Long id;
    private String title;
    private Long teacherId;
    private String teacherName;
    private Integer status;
    private Integer finishStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

