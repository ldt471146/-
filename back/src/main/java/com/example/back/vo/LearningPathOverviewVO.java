package com.example.back.vo;

import lombok.Data;

import java.util.List;

/**
 * 学习路径总览
 */
@Data
public class LearningPathOverviewVO {
    private Long courseId;
    private Integer totalPoints;
    private Integer learnedPoints;
    private Long nextPointId;
    private String nextPointTitle;
    private List<LearningPathPointVO> points;
}

