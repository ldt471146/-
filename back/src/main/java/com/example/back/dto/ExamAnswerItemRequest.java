package com.example.back.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 考试单题答案
 */
@Data
public class ExamAnswerItemRequest {

    @NotNull(message = "题目ID不能为空")
    private Long questionId;

    private List<String> answers;
}

