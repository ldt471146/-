package com.example.back.dto;

import lombok.Data;

/**
 * 教师维护知识点依赖
 */
@Data
public class TeacherKnowledgeDependencyRequest {
    private Long fromPointId;
    private Long toPointId;
}

