package com.example.back.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 提交模拟考试请求
 */
@Data
public class ExamSubmitRequest {

    @NotBlank(message = "考试ID不能为空")
    private String examId;

    @Valid
    private List<ExamAnswerItemRequest> answers;
}

