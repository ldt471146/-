package com.example.back.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 考试提交记录视图
 */
@Data
public class ExamSubmissionVO {
    private Long id;
    private Long taskId;
    private String taskTitle;
    private Integer score;
    private Integer totalCount;
    private Integer correctCount;
    private LocalDateTime submittedAt;
}

