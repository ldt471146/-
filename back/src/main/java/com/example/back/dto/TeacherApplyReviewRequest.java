package com.example.back.dto;

import lombok.Data;

/**
 * 教师申请审核
 */
@Data
public class TeacherApplyReviewRequest {
    /**
     * 状态：1通过，2拒绝
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
