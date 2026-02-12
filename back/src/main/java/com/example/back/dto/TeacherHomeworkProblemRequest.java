package com.example.back.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 作业题目请求
 */
@Data
public class TeacherHomeworkProblemRequest {

    @NotNull(message = "题目ID不能为空")
    private Long problemId;

    @NotNull(message = "分值不能为空")
    @Min(value = 1, message = "分值至少为 1")
    private Integer score;
}

