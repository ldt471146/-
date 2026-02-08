package com.example.back.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新学习进度请求
 */
@Data
public class LearnProgressRequest {

    @NotNull(message = "课时ID不能为空")
    private Long lessonId;

    @NotNull(message = "进度不能为空")
    @Min(value = 0, message = "进度最小为0")
    @Max(value = 100, message = "进度最大为100")
    private Integer progress;

    /**
     * 本次学习时长（秒）
     */
    private Integer durationSeconds;
}
