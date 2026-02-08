package com.example.back.vo;

import lombok.Data;

import java.util.List;

/**
 * 成长趋势
 */
@Data
public class ReportTrendVO {
    private List<String> days;
    private List<Integer> learnMinutes;
    private List<Integer> questionTotal;
    private List<Integer> questionCorrect;
}
