package com.example.back.vo;

import lombok.Data;

/**
 * 教师端考试任务统计
 */
@Data
public class TeacherExamStatVO {
    private Long taskId;
    private String taskTitle;
    private Integer attempts;
    private Integer passCount;
    private Integer passRate;
}

