package com.example.back.service;

import com.example.back.dto.LoginRequest;
import com.example.back.dto.RegisterRequest;
import com.example.back.vo.AuthVO;
import com.example.back.vo.UserVO;

/**
 * 认证服务
 */
public interface AuthService {

    AuthVO register(RegisterRequest request);

    AuthVO login(LoginRequest request);

    UserVO currentUser();

    void sendEmailCode(String email);
}
