package com.example.back.vo;

import lombok.Data;

import java.util.List;

/**
 * 创建模拟考试响应
 */
@Data
public class ExamCreateVO {
    private String examId;
    private Integer durationMinutes;
    private Integer total;
    private List<ExamQuestionVO> questions;
}

