package com.example.back.controller;

import com.example.back.common.ApiResponse;
import com.example.back.dto.EmailCodeRequest;
import com.example.back.dto.LoginRequest;
import com.example.back.dto.RegisterRequest;
import com.example.back.service.AuthService;
import com.example.back.vo.AuthVO;
import com.example.back.vo.UserVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 认证接口
 */
@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 邮箱注册（默认学生角色）
     */
    @PostMapping("/auth/register")
    public ApiResponse<AuthVO> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register(request));
    }

    /**
     * 发送邮箱验证码
     */
    @PostMapping("/auth/send-code")
    public ApiResponse<Void> sendCode(@Valid @RequestBody EmailCodeRequest request) {
        authService.sendEmailCode(request.getEmail());
        return ApiResponse.ok(null);
    }

    /**
     * 邮箱登录
     */
    @PostMapping("/auth/login")
    public ApiResponse<AuthVO> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    /**
     * 当前用户信息
     */
    @GetMapping("/user/me")
    public ApiResponse<UserVO> me() {
        return ApiResponse.ok(authService.currentUser());
    }
}
