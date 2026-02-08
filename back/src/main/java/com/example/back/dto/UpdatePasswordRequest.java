package com.example.back.dto;

import lombok.Data;

/**
 * 修改密码
 */
@Data
public class UpdatePasswordRequest {
    private String oldPassword;
    private String newPassword;
    private String code;
}
