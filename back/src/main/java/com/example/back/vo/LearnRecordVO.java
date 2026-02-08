package com.example.back.vo;

import lombok.Data;

/**
 * 学习记录返回
 */
@Data
public class LearnRecordVO {
    private Long lessonId;
    private Integer progress;
    private Integer isFinished;
    private Integer learnSeconds;
}
