package com.example.back.vo;

import lombok.Data;

/**
 * 教师端课程统计
 */
@Data
public class TeacherCourseStatVO {
    private Long courseId;
    private String courseTitle;
    private Integer studentCount;
    private Double avgScore;
    private Integer totalLearnMinutes;
}

