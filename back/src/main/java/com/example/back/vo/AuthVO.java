package com.example.back.vo;

import lombok.Data;

/**
 * 登录/注册返回
 */
@Data
public class AuthVO {
    private String token;
    private UserVO user;
}
