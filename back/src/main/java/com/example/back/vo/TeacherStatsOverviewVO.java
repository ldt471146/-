package com.example.back.vo;

import lombok.Data;

import java.util.List;

/**
 * 教师统计总览
 */
@Data
public class TeacherStatsOverviewVO {
    private Integer totalCourses;
    private Integer totalStudents;
    private Integer totalSubmissions;
    private Double avgScore;
    private List<TeacherStudentRankVO> studentRanks;
    private List<TeacherCourseStatVO> courseStats;
    private List<TeacherExamStatVO> examStats;
}

