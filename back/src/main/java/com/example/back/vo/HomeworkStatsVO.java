package com.example.back.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作业统计
 */
@Data
public class HomeworkStatsVO {

    /**
     * 应参与人数（课程在读学生）
     */
    private Integer expectedStudents;

    /**
     * 已参与人数（至少提交一次）
     */
    private Integer activeStudents;

    /**
     * 提交总次数（按题目作答记录计）
     */
    private Integer submissionCount;

    /**
     * 正确率（0-100）
     */
    private Double accuracy;

    /**
     * 最近提交时间
     */
    private LocalDateTime lastSubmitAt;
}

