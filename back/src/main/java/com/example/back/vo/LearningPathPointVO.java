package com.example.back.vo;

import lombok.Data;

import java.util.List;

/**
 * 学习路径节点
 */
@Data
public class LearningPathPointVO {
    private Long pointId;
    private Long chapterId;
    private String chapterTitle;
    private String title;
    private String description;
    private Integer sortNo;
    /**
     * LEARNED / UNLOCKED / LOCKED
     */
    private String status;
    private List<Long> prerequisitePointIds;
}

