package com.example.back.dto;

import lombok.Data;

/**
 * 管理员课程审核请求
 */
@Data
public class AdminCourseReviewRequest {
    /**
     * 1-通过(上架) 0-拒绝(下架)
     */
    private Integer status;
    private String remark;
}

