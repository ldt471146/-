package com.example.back.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.back.common.ApiResponse;
import com.example.back.dto.AdminCourseReviewRequest;
import com.example.back.entity.EduCourse;
import com.example.back.entity.SysUser;
import com.example.back.mapper.EduCourseMapper;
import com.example.back.mapper.SysUserMapper;
import com.example.back.vo.AdminCourseVO;
import com.example.back.vo.PageResultVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 管理员-课程审核
 */
@RestController
@RequestMapping("/api/admin/courses")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCourseController {

    private final EduCourseMapper courseMapper;
    private final SysUserMapper userMapper;

    public AdminCourseController(EduCourseMapper courseMapper, SysUserMapper userMapper) {
        this.courseMapper = courseMapper;
        this.userMapper = userMapper;
    }

    @GetMapping
    public ApiResponse<PageResultVO<AdminCourseVO>> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "page", defaultValue = "1") long page,
            @RequestParam(value = "size", defaultValue = "10") long size
    ) {
        Page<EduCourse> mpPage = new Page<>(page, size);
        LambdaQueryWrapper<EduCourse> wrapper = new LambdaQueryWrapper<EduCourse>()
                .eq(status != null, EduCourse::getStatus, status)
                .orderByDesc(EduCourse::getId);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(EduCourse::getTitle, keyword.trim());
        }
        Page<EduCourse> result = courseMapper.selectPage(mpPage, wrapper);

        Set<Long> teacherIds = result.getRecords().stream()
                .map(EduCourse::getTeacherId)
                .filter(v -> v != null)
                .collect(Collectors.toSet());
        Map<Long, String> teacherMap = userMapper.selectBatchIds(teacherIds).stream()
                .collect(Collectors.toMap(SysUser::getId, SysUser::getUsername));

        List<AdminCourseVO> records = result.getRecords().stream().map(c -> {
            AdminCourseVO vo = new AdminCourseVO();
            vo.setId(c.getId());
            vo.setTitle(c.getTitle());
            vo.setTeacherId(c.getTeacherId());
            vo.setTeacherName(teacherMap.getOrDefault(c.getTeacherId(), "-"));
            vo.setStatus(c.getStatus());
            vo.setFinishStatus(c.getFinishStatus());
            vo.setCreatedAt(c.getCreatedAt());
            vo.setUpdatedAt(c.getUpdatedAt());
            return vo;
        }).collect(Collectors.toList());

        PageResultVO<AdminCourseVO> vo = new PageResultVO<>();
        vo.setPage(page);
        vo.setSize(size);
        vo.setTotal(result.getTotal());
        vo.setRecords(records);
        return ApiResponse.ok(vo);
    }

    @PostMapping("/{id}/review")
    public ApiResponse<Void> review(@PathVariable("id") Long id, @RequestBody AdminCourseReviewRequest request) {
        if (request.getStatus() == null || (request.getStatus() != 0 && request.getStatus() != 1)) {
            throw new IllegalArgumentException("审核状态不合法");
        }
        EduCourse course = courseMapper.selectById(id);
        if (course == null) {
            throw new IllegalArgumentException("课程不存在");
        }
        course.setStatus(request.getStatus());
        courseMapper.updateById(course);
        return ApiResponse.ok();
    }
}

