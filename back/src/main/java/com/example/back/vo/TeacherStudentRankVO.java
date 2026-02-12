package com.example.back.vo;

import lombok.Data;

/**
 * 教师端学生排行
 */
@Data
public class TeacherStudentRankVO {
    private Long userId;
    private String username;
    private Integer learnMinutes;
    private Double avgScore;
    private Integer submissionCount;
}

