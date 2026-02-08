package com.example.back.vo;

import lombok.Data;

import java.util.List;

/**
 * 提交考试响应
 */
@Data
public class ExamSubmitVO {
    private Integer total;
    private Integer correctCount;
    private Integer wrongCount;
    private Integer score;
    private List<ExamQuestionResultVO> results;
}

