package com.example.back.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 题库导入请求
 */
@Data
public class TeacherQuestionImportRequest {

    @NotNull(message = "课程不能为空")
    private Long courseId;

    private Long chapterId;

    private List<TeacherQuestionImportItemRequest> items;
}

