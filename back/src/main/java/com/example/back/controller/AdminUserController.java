package com.example.back.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.back.common.ApiResponse;
import com.example.back.dto.AdminUserRolesRequest;
import com.example.back.dto.AdminUserStatusRequest;
import com.example.back.entity.SysRole;
import com.example.back.entity.SysUser;
import com.example.back.entity.SysUserRole;
import com.example.back.mapper.SysRoleMapper;
import com.example.back.mapper.SysUserMapper;
import com.example.back.mapper.SysUserRoleMapper;
import com.example.back.util.SecurityUtil;
import com.example.back.vo.AdminUserVO;
import com.example.back.vo.PageResultVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 管理员-用户管理
 */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;

    public AdminUserController(SysUserMapper userMapper, SysRoleMapper roleMapper, SysUserRoleMapper userRoleMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
    }

    @GetMapping
    public ApiResponse<PageResultVO<AdminUserVO>> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "page", defaultValue = "1") long page,
            @RequestParam(value = "size", defaultValue = "10") long size
    ) {
        Page<SysUser> mpPage = new Page<>(page, size);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(status != null, SysUser::getStatus, status)
                .orderByDesc(SysUser::getId);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword.trim())
                    .or()
                    .like(SysUser::getEmail, keyword.trim())
                    .or()
                    .like(SysUser::getPhone, keyword.trim()));
        }
        Page<SysUser> result = userMapper.selectPage(mpPage, wrapper);
        List<AdminUserVO> records = result.getRecords().stream().map(this::toUserVO).collect(Collectors.toList());

        PageResultVO<AdminUserVO> vo = new PageResultVO<>();
        vo.setPage(page);
        vo.setSize(size);
        vo.setTotal(result.getTotal());
        vo.setRecords(records);
        return ApiResponse.ok(vo);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable("id") Long id, @RequestBody AdminUserStatusRequest request) {
        if (request.getStatus() == null || (request.getStatus() != 0 && request.getStatus() != 1)) {
            throw new IllegalArgumentException("状态不合法");
        }
        Long currentUserId = SecurityUtil.getUserId();
        if (currentUserId != null && currentUserId.equals(id) && request.getStatus() == 0) {
            throw new IllegalArgumentException("不能禁用当前管理员账号");
        }
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        user.setStatus(request.getStatus());
        userMapper.updateById(user);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/roles")
    public ApiResponse<Void> updateRoles(@PathVariable("id") Long id, @RequestBody AdminUserRolesRequest request) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        List<String> roleCodes = request.getRoleCodes();
        if (roleCodes == null || roleCodes.isEmpty()) {
            throw new IllegalArgumentException("至少保留一个角色");
        }
        Set<String> normalizedCodes = roleCodes.stream()
                .filter(v -> v != null && !v.isBlank())
                .map(String::trim)
                .collect(Collectors.toSet());
        if (normalizedCodes.isEmpty()) {
            throw new IllegalArgumentException("角色不能为空");
        }
        Long currentUserId = SecurityUtil.getUserId();
        if (currentUserId != null && currentUserId.equals(id) && !normalizedCodes.contains("ADMIN")) {
            throw new IllegalArgumentException("当前管理员必须保留 ADMIN 角色");
        }

        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, id));

        for (String code : normalizedCodes) {
            SysRole role = roleMapper.selectByCode(code);
            if (role == null) {
                role = new SysRole();
                role.setCode(code);
                role.setName(code);
                roleMapper.insert(role);
            }
            SysUserRole link = new SysUserRole();
            link.setUserId(id);
            link.setRoleId(role.getId());
            link.setIsDeleted(0);
            userRoleMapper.insert(link);
        }
        return ApiResponse.ok();
    }

    private AdminUserVO toUserVO(SysUser user) {
        AdminUserVO vo = new AdminUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setStatus(user.getStatus());
        vo.setMuteStatus(user.getMuteStatus());
        vo.setBanReason(user.getBanReason());
        vo.setRoles(roleMapper.selectRoleCodesByUserId(user.getId()));
        vo.setCreatedAt(user.getCreatedAt());
        vo.setUpdatedAt(user.getUpdatedAt());
        return vo;
    }
}
