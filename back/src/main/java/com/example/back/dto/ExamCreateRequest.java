package com.example.back.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建模拟考试请求
 */
@Data
public class ExamCreateRequest {

    @NotNull(message = "课程不能为空")
    private Long courseId;

    private Long chapterId;

    @Min(value = 1, message = "题目数量最少为 1")
    @Max(value = 50, message = "题目数量最多为 50")
    private Integer questionCount = 10;

    @Min(value = 5, message = "时长最少为 5 分钟")
    @Max(value = 180, message = "时长最多为 180 分钟")
    private Integer durationMinutes = 30;
}

