package com.example.back.dto;

import lombok.Data;

/**
 * 教师端章节请求
 */
@Data
public class TeacherChapterRequest {
    private String title;
    private Integer sortNo;
}
