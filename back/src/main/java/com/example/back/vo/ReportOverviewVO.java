package com.example.back.vo;

import lombok.Data;

/**
 * 成长报告概览
 */
@Data
public class ReportOverviewVO {
    private Integer totalCourses;
    private Integer myCourses;
    private Integer totalLessons;
    private Integer finishedLessons;
    private Integer learnSeconds;
    private Integer questionTotal;
    private Integer questionCorrect;
    private Integer questionAccuracy;
    private Integer wrongCount;
    private Integer wrongRedoCount;
    private Integer favoriteCount;
    private java.util.List<ReportWeakTagVO> weakCourses;
}
