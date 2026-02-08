package com.example.back.vo;

import lombok.Data;

/**
 * 学习时长趋势
 */
@Data
public class ReportLearnTrendVO {
    private String day;
    private Integer learnSeconds;
}
