package com.example.back.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.back.common.ApiResponse;
import com.example.back.dto.TeacherApplyReviewRequest;
import com.example.back.entity.SysRole;
import com.example.back.entity.SysNotice;
import com.example.back.entity.SysNoticeUser;
import com.example.back.entity.SysTeacherApply;
import com.example.back.entity.SysUserRole;
import com.example.back.mapper.SysRoleMapper;
import com.example.back.mapper.SysNoticeMapper;
import com.example.back.mapper.SysNoticeUserMapper;
import com.example.back.mapper.SysTeacherApplyMapper;
import com.example.back.mapper.SysUserRoleMapper;
import com.example.back.mapper.SysUserMapper;
import com.example.back.vo.TeacherApplyVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教师申请审核（管理员）
 */
@RestController
@RequestMapping("/api/admin/teacher-apply")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTeacherApplyController {

    private final SysTeacherApplyMapper applyMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysNoticeMapper noticeMapper;
    private final SysNoticeUserMapper noticeUserMapper;
    private final SysUserMapper userMapper;

    public AdminTeacherApplyController(SysTeacherApplyMapper applyMapper,
                                       SysRoleMapper roleMapper,
                                       SysUserRoleMapper userRoleMapper,
                                       SysNoticeMapper noticeMapper,
                                       SysNoticeUserMapper noticeUserMapper,
                                       SysUserMapper userMapper) {
        this.applyMapper = applyMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.noticeMapper = noticeMapper;
        this.noticeUserMapper = noticeUserMapper;
        this.userMapper = userMapper;
    }

    @GetMapping
    public ApiResponse<List<TeacherApplyVO>> list(@RequestParam(value = "status", required = false) Integer status) {
        List<SysTeacherApply> applies = applyMapper.selectList(new LambdaQueryWrapper<SysTeacherApply>()
                .eq(status != null, SysTeacherApply::getStatus, status)
                .orderByDesc(SysTeacherApply::getId));
        if (applies == null || applies.isEmpty()) {
            return ApiResponse.ok(List.of());
        }
        List<Long> userIds = applies.stream().map(SysTeacherApply::getUserId).distinct().toList();
        java.util.Map<Long, com.example.back.entity.SysUser> userMap = userMapper.selectBatchIds(userIds)
                .stream()
                .collect(java.util.stream.Collectors.toMap(com.example.back.entity.SysUser::getId, u -> u));
        List<TeacherApplyVO> result = new java.util.ArrayList<>();
        for (SysTeacherApply apply : applies) {
            com.example.back.entity.SysUser user = userMap.get(apply.getUserId());
            TeacherApplyVO vo = new TeacherApplyVO();
            vo.setId(apply.getId());
            vo.setUserId(apply.getUserId());
            vo.setUsername(user == null ? "-" : user.getUsername());
            vo.setEmail(user == null ? "-" : user.getEmail());
            vo.setStatus(apply.getStatus());
            vo.setRemark(apply.getRemark());
            vo.setCreatedAt(apply.getCreatedAt());
            result.add(vo);
        }
        return ApiResponse.ok(result);
    }

    @PostMapping("/{id}/review")
    public ApiResponse<Void> review(@PathVariable("id") Long id, @RequestBody TeacherApplyReviewRequest request) {
        SysTeacherApply apply = applyMapper.selectById(id);
        if (apply == null) {
            throw new IllegalArgumentException("申请不存在");
        }
        if (apply.getStatus() != null && apply.getStatus() != 0) {
            throw new IllegalArgumentException("申请已处理");
        }
        if (request.getStatus() == null || (request.getStatus() != 1 && request.getStatus() != 2)) {
            throw new IllegalArgumentException("状态不合法");
        }
        apply.setStatus(request.getStatus());
        apply.setRemark(request.getRemark());
        applyMapper.updateById(apply);

        if (request.getStatus() == 1) {
            SysRole role = roleMapper.selectByCode("TEACHER");
            if (role == null) {
                role = new SysRole();
                role.setCode("TEACHER");
                role.setName("教师");
                roleMapper.insert(role);
            }
            Long count = userRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>()
                    .eq(SysUserRole::getUserId, apply.getUserId())
                    .eq(SysUserRole::getRoleId, role.getId()));
            if (count == null || count == 0L) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(apply.getUserId());
                ur.setRoleId(role.getId());
                userRoleMapper.insert(ur);
            }
        }
        sendNotice(apply.getUserId(), request.getStatus(), request.getRemark());
        return ApiResponse.ok();
    }

    private void sendNotice(Long userId, Integer status, String remark) {
        com.example.back.entity.SysUser user = userMapper.selectById(userId);
        String username = user == null ? "用户" : user.getUsername();
        SysNotice notice = new SysNotice();
        notice.setType("system");
        notice.setStatus(1);
        if (status != null && status == 1) {
            notice.setTitle("教师申请审核通过");
            notice.setContent("用户：" + username + "（ID：" + userId + "）教师申请已通过审核。" +
                    (remark == null || remark.isBlank() ? "" : (" 备注：" + remark)));
        } else {
            notice.setTitle("教师申请审核未通过");
            notice.setContent("用户：" + username + "（ID：" + userId + "）教师申请未通过审核。" +
                    (remark == null || remark.isBlank() ? "" : (" 备注：" + remark)));
        }
        noticeMapper.insert(notice);

        SysNoticeUser nu = new SysNoticeUser();
        nu.setUserId(userId);
        nu.setNoticeId(notice.getId());
        nu.setIsRead(0);
        nu.setIsDeleted(0);
        noticeUserMapper.insert(nu);
    }
}
