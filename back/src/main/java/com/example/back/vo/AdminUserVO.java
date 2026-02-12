package com.example.back.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员用户管理视图
 */
@Data
public class AdminUserVO {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private Integer status;
    private List<String> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

