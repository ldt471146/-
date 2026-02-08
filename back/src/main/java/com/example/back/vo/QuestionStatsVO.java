package com.example.back.vo;

import lombok.Data;

import java.util.List;

/**
 * 题库统计
 */
@Data
public class QuestionStatsVO {
    private Integer total;
    private Integer correct;
    private Integer accuracy;
    private Integer wrongCount;
    private Integer favoriteCount;
    private Integer wrongRedoCount;
    private List<QuestionMiniVO> recentWrong;
}
