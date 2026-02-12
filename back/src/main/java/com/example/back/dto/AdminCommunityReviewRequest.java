package com.example.back.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员社区审核请求
 */
@Data
public class AdminCommunityReviewRequest {

    /**
     * APPROVE / DELETE / MUTE_USER
     */
    @NotBlank(message = "审核动作不能为空")
    private String action;

    @Size(max = 255, message = "原因长度不能超过255")
    private String reason;
}
