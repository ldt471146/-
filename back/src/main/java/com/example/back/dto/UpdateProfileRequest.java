package com.example.back.dto;

import lombok.Data;

/**
 * 更新个人资料
 */
@Data
public class UpdateProfileRequest {
    private String username;
    private String avatar;
    private String phone;
}
