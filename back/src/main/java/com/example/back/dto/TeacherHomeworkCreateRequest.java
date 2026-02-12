package com.example.back.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 教师创建作业请求
 */
@Data
public class TeacherHomeworkCreateRequest {

    @NotNull(message = "课程不能为空")
    private Long courseId;

    @NotBlank(message = "作业标题不能为空")
    private String title;

    private LocalDateTime deadline;

    @Valid
    private List<TeacherHomeworkProblemRequest> problems;
}

