package com.example.back.dto;

import lombok.Data;

import java.util.List;

/**
 * 提交答案请求
 */
@Data
public class QuestionSubmitRequest {

    private Long questionId;

    /**
     * 用户选择的答案（单选/多选）
     */
    private List<String> answers;
}
