package com.example.back.dto;

import lombok.Data;

/**
 * 学习路径进度上报
 */
@Data
public class LearningPathProgressRequest {
    private Long courseId;
    private Long pointId;
    private Integer status;
    private Integer score;
}

