package com.example.back.dto;

import lombok.Data;

/**
 * 教师端课时请求
 */
@Data
public class TeacherLessonRequest {
    private String title;
    private String contentType;
    private String contentUrl;
    private String contentText;
    private Integer sortNo;
}
