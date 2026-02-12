package com.example.back.dto;

import lombok.Data;

/**
 * 教师维护知识点
 */
@Data
public class TeacherKnowledgePointRequest {
    private Long chapterId;
    private String title;
    private String description;
    private Integer sortNo;
    private Integer status;
}

