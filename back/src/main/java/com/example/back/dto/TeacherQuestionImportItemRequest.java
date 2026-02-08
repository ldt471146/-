package com.example.back.dto;

import lombok.Data;

import java.util.List;

/**
 * 题目导入单项
 */
@Data
public class TeacherQuestionImportItemRequest {
    private String title;
    private String type;
    private String analysis;
    private Integer difficulty;
    private List<TeacherQuestionOptionRequest> options;
}

