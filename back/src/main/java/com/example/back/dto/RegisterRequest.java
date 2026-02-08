package com.example.back.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求
 */
@Data
public class RegisterRequest {

    /**
     * 邮箱（登录账号）
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 用户名（展示用）
     */
    @NotBlank(message = "用户名不能为空")
    @Size(max = 64, message = "用户名过长")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度为6-32位")
    private String password;

    /**
     * 邮箱验证码
     */
    @NotBlank(message = "验证码不能为空")
    private String code;

    /**
     * 角色（可选）：STUDENT/TEACHER
     */
    private String roleCode;
}
