package com.example.back.dto;

import lombok.Data;

/**
 * 教师端课程请求
 */
@Data
public class TeacherCourseRequest {
    private String title;
    private String cover;
    private String intro;
    private Integer status;
    private Integer finishStatus;
}
