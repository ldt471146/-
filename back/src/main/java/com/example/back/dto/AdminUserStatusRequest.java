package com.example.back.dto;

import lombok.Data;

/**
 * 管理员修改用户状态
 */
@Data
public class AdminUserStatusRequest {
    /**
     * 1-正常，0-禁用
     */
    private Integer status;
}

