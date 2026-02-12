package com.example.back.dto;

import lombok.Data;

import java.util.List;

/**
 * 管理员修改用户角色
 */
@Data
public class AdminUserRolesRequest {
    private List<String> roleCodes;
}

