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
import com.example.back.vo.AdminUserOverviewVO;
import com.example.back.vo.AdminUserVO;
import com.example.back.vo.PageResultVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 管理员用户治理
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
            @RequestParam(value = "role", required = false) String roleCode,
            @RequestParam(value = "page", defaultValue = "1") long page,
            @RequestParam(value = "size", defaultValue = "10") long size
    ) {
        List<Long> scopedUserIds = resolveRoleUserIds(roleCode);
        if (hasRoleFilter(roleCode) && scopedUserIds.isEmpty()) {
            return ApiResponse.ok(emptyPage(page, size));
        }
        Page<SysUser> result = userMapper.selectPage(new Page<>(page, size), buildUserWrapper(keyword, status, scopedUserIds));
        return ApiResponse.ok(buildPageResult(result, page, size));
    }

    @GetMapping("/overview")
    public ApiResponse<AdminUserOverviewVO> overview(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "role", required = false) String roleCode
    ) {
        List<Long> scopedUserIds = resolveRoleUserIds(roleCode);
        if (hasRoleFilter(roleCode) && scopedUserIds.isEmpty()) {
            return ApiResponse.ok(new AdminUserOverviewVO());
        }
        List<SysUser> users = userMapper.selectList(buildUserWrapper(keyword, status, scopedUserIds));
        Map<Long, List<String>> rolesByUserId = loadRolesByUserIds(users.stream().map(SysUser::getId).toList());
        return ApiResponse.ok(buildOverview(users, rolesByUserId));
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
                .map(String::toUpperCase)
                .collect(Collectors.toSet());
        if (normalizedCodes.isEmpty()) {
            throw new IllegalArgumentException("角色不能为空");
        }
        Long currentUserId = SecurityUtil.getUserId();
        if (currentUserId != null && currentUserId.equals(id) && !normalizedCodes.contains("ADMIN")) {
            throw new IllegalArgumentException("当前管理员必须保留 ADMIN 角色");
        }

        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
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

    private boolean hasRoleFilter(String roleCode) {
        return roleCode != null && !roleCode.isBlank();
    }

    private LambdaQueryWrapper<SysUser> buildUserWrapper(String keyword, Integer status, List<Long> scopedUserIds) {
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
        if (scopedUserIds != null) {
            wrapper.in(SysUser::getId, scopedUserIds);
        }
        return wrapper;
    }

    private List<Long> resolveRoleUserIds(String roleCode) {
        if (!hasRoleFilter(roleCode)) {
            return null;
        }
        SysRole role = roleMapper.selectByCode(roleCode.trim().toUpperCase());
        if (role == null) {
            return List.of();
        }
        return userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getRoleId, role.getId()))
                .stream()
                .map(SysUserRole::getUserId)
                .distinct()
                .toList();
    }

    private PageResultVO<AdminUserVO> buildPageResult(Page<SysUser> result, long page, long size) {
        List<Long> userIds = result.getRecords().stream().map(SysUser::getId).toList();
        Map<Long, List<String>> rolesByUserId = loadRolesByUserIds(userIds);
        List<AdminUserVO> records = result.getRecords().stream()
                .map(user -> toUserVO(user, rolesByUserId.getOrDefault(user.getId(), List.of())))
                .toList();

        PageResultVO<AdminUserVO> vo = new PageResultVO<>();
        vo.setPage(page);
        vo.setSize(size);
        vo.setTotal(result.getTotal());
        vo.setRecords(records);
        return vo;
    }

    private PageResultVO<AdminUserVO> emptyPage(long page, long size) {
        PageResultVO<AdminUserVO> vo = new PageResultVO<>();
        vo.setPage(page);
        vo.setSize(size);
        vo.setTotal(0L);
        vo.setRecords(List.of());
        return vo;
    }

    private Map<Long, List<String>> loadRolesByUserIds(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        List<SysUserRole> links = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .in(SysUserRole::getUserId, userIds));
        Map<Long, List<String>> rolesByUserId = new HashMap<>();
        userIds.forEach(userId -> rolesByUserId.put(userId, new ArrayList<>()));
        if (links.isEmpty()) {
            return rolesByUserId;
        }

        Set<Long> roleIds = links.stream().map(SysUserRole::getRoleId).collect(Collectors.toSet());
        Map<Long, String> roleCodeById = roleMapper.selectBatchIds(roleIds).stream()
                .collect(Collectors.toMap(SysRole::getId, SysRole::getCode));
        for (SysUserRole link : links) {
            String code = roleCodeById.get(link.getRoleId());
            if (code != null) {
                rolesByUserId.computeIfAbsent(link.getUserId(), key -> new ArrayList<>()).add(code);
            }
        }
        return rolesByUserId;
    }

    private AdminUserOverviewVO buildOverview(List<SysUser> users, Map<Long, List<String>> rolesByUserId) {
        AdminUserOverviewVO vo = new AdminUserOverviewVO();
        vo.setTotalUsers((long) users.size());
        vo.setActiveUsers(users.stream().filter(user -> user.getStatus() != null && user.getStatus() == 1).count());
        vo.setDisabledUsers(users.stream().filter(user -> user.getStatus() == null || user.getStatus() != 1).count());
        vo.setMutedUsers(users.stream().filter(user -> user.getMuteStatus() != null && user.getMuteStatus() == 1).count());
        vo.setMultiRoleUsers(rolesByUserId.values().stream().filter(roles -> roles.size() > 1).count());
        vo.setStudentUsers(countUsersByRole(rolesByUserId, "STUDENT"));
        vo.setTeacherUsers(countUsersByRole(rolesByUserId, "TEACHER"));
        vo.setAdminUsers(countUsersByRole(rolesByUserId, "ADMIN"));
        return vo;
    }

    private long countUsersByRole(Map<Long, List<String>> rolesByUserId, String roleCode) {
        return rolesByUserId.values().stream().filter(roles -> roles.contains(roleCode)).count();
    }

    private AdminUserVO toUserVO(SysUser user, List<String> roles) {
        AdminUserVO vo = new AdminUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setStatus(user.getStatus());
        vo.setMuteStatus(user.getMuteStatus());
        vo.setBanReason(user.getBanReason());
        vo.setRoles(roles);
        vo.setCreatedAt(user.getCreatedAt());
        vo.setUpdatedAt(user.getUpdatedAt());
        return vo;
    }
}