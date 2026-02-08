package com.example.back.vo;

import lombok.Data;

import java.util.List;

/**
 * 用户信息返回
 */
@Data
public class UserVO {
    private Long id;
    private String username;
    private String email;
    private String avatar;
    private Integer status;
    private List<String> roles;
}
