package com.example.back.vo;

import lombok.Data;

import java.util.List;

/**
 * 教师端题目
 */
@Data
public class TeacherQuestionVO {
    private Long id;
    private String title;
    private String type;
    private Integer difficulty;
    private Long courseId;
    private Long chapterId;
    private String analysis;
    private List<TeacherQuestionOptionVO> options;
}
