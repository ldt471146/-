package com.example.back.dto;

import lombok.Data;

import java.util.List;

/**
 * 教师端题目请求
 */
@Data
public class TeacherQuestionRequest {
    private String title;
    private String type;
    private String analysis;
    private Integer difficulty;
    private Long courseId;
    private Long chapterId;
    private List<TeacherQuestionOptionRequest> options;
}
