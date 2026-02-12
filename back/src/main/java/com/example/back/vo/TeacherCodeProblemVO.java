package com.example.back.vo;

import lombok.Data;

import java.util.List;

/**
 * 教师端编程题
 */
@Data
public class TeacherCodeProblemVO {
    private Long id;
    private Long courseId;
    private Long chapterId;
    private String title;
    private String content;
    private Integer difficulty;
    private Integer timeLimit;
    private Integer memoryLimit;
    private Integer status;
    private List<TeacherCodeTestcaseVO> testcases;
}

