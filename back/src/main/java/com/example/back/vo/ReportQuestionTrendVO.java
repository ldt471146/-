package com.example.back.vo;

import lombok.Data;

/**
 * 题目趋势
 */
@Data
public class ReportQuestionTrendVO {
    private String day;
    private Integer total;
    private Integer correct;
}
