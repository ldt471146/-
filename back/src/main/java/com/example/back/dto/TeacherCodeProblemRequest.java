package com.example.back.dto;

import lombok.Data;

import java.util.List;

/**
 * 教师端编程题请求
 */
@Data
public class TeacherCodeProblemRequest {
    private String title;
    private String content;
    private Integer difficulty;
    private Integer timeLimit;
    private Integer memoryLimit;
    private Integer status;
    private Long courseId;
    private Long chapterId;
    private List<TeacherCodeTestcaseRequest> testcases;
}

