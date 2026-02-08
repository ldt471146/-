package com.example.back.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 教师创建考试任务请求
 */
@Data
public class TeacherExamTaskCreateRequest {

    @NotNull(message = "课程不能为空")
    private Long courseId;

    private Long chapterId;

    @NotBlank(message = "考试标题不能为空")
    private String title;

    @Min(value = 1, message = "题目数量至少 1")
    @Max(value = 50, message = "题目数量最多 50")
    private Integer questionCount = 10;

    @Min(value = 5, message = "考试时长至少 5 分钟")
    @Max(value = 180, message = "考试时长最多 180 分钟")
    private Integer durationMinutes = 30;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}

