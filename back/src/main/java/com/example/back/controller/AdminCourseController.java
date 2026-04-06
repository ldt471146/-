package com.example.back.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.back.common.ApiResponse;
import com.example.back.dto.AdminCourseReviewRequest;
import com.example.back.entity.EduCourse;
import com.example.back.entity.SysUser;
import com.example.back.mapper.EduCourseMapper;
import com.example.back.mapper.SysUserMapper;
import com.example.back.service.AuditLogService;
import com.example.back.vo.AdminCourseOverviewVO;
import com.example.back.vo.AdminCourseVO;
import com.example.back.vo.PageResultVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 管理员课程审核与治理
 */
@RestController
@RequestMapping("/api/admin/courses")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCourseController {

    private final EduCourseMapper courseMapper;
    private final SysUserMapper userMapper;
    private final AuditLogService auditLogService;

    public AdminCourseController(EduCourseMapper courseMapper,
                                 SysUserMapper userMapper,
                                 AuditLogService auditLogService) {
        this.courseMapper = courseMapper;
        this.userMapper = userMapper;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ApiResponse<PageResultVO<AdminCourseVO>> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "page", defaultValue = "1") long page,
            @RequestParam(value = "size", defaultValue = "10") long size
    ) {
        Page<EduCourse> result = courseMapper.selectPage(new Page<>(page, size), buildCourseWrapper(keyword, status, true));
        return ApiResponse.ok(toPageResult(result, page, size));
    }

    @GetMapping("/overview")
    public ApiResponse<AdminCourseOverviewVO> overview(
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        List<EduCourse> courses = courseMapper.selectList(buildCourseWrapper(keyword, null, false));
        AdminCourseOverviewVO vo = new AdminCourseOverviewVO();
        vo.setTotalCourses((long) courses.size());
        vo.setPublishedCourses(courses.stream().filter(course -> course.getStatus() != null && course.getStatus() == 1).count());
        vo.setUnpublishedCourses(courses.stream().filter(course -> course.getStatus() == null || course.getStatus() != 1).count());
        vo.setFinishedCourses(courses.stream().filter(course -> course.getFinishStatus() != null && course.getFinishStatus() == 1).count());
        vo.setUpdatingCourses(courses.stream().filter(course -> course.getFinishStatus() == null || course.getFinishStatus() != 1).count());
        vo.setTeacherCount(courses.stream().map(EduCourse::getTeacherId).filter(id -> id != null).distinct().count());
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
        auditLogService.log(
                "COURSE_REVIEW",
                request.getStatus() == 1 ? "APPROVE" : "REJECT",
                "COURSE",
                id,
                "课程审核状态变更为: " + request.getStatus()
        );
        return ApiResponse.ok();
    }

    private LambdaQueryWrapper<EduCourse> buildCourseWrapper(String keyword, Integer status, boolean withOrder) {
        LambdaQueryWrapper<EduCourse> wrapper = new LambdaQueryWrapper<EduCourse>()
                .eq(status != null, EduCourse::getStatus, status);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(EduCourse::getTitle, keyword.trim());
        }
        if (withOrder) {
            wrapper.orderByDesc(EduCourse::getId);
        }
        return wrapper;
    }

    private PageResultVO<AdminCourseVO> toPageResult(Page<EduCourse> result, long page, long size) {
        Set<Long> teacherIds = result.getRecords().stream()
                .map(EduCourse::getTeacherId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, String> teacherMap = teacherIds.isEmpty()
                ? Map.of()
                : userMapper.selectBatchIds(teacherIds).stream().collect(Collectors.toMap(SysUser::getId, SysUser::getUsername));

        List<AdminCourseVO> records = result.getRecords().stream().map(course -> {
            AdminCourseVO vo = new AdminCourseVO();
            vo.setId(course.getId());
            vo.setTitle(course.getTitle());
            vo.setTeacherId(course.getTeacherId());
            vo.setTeacherName(teacherMap.getOrDefault(course.getTeacherId(), "-"));
            vo.setStatus(course.getStatus());
            vo.setFinishStatus(course.getFinishStatus());
            vo.setCreatedAt(course.getCreatedAt());
            vo.setUpdatedAt(course.getUpdatedAt());
            return vo;
        }).toList();

        PageResultVO<AdminCourseVO> vo = new PageResultVO<>();
        vo.setPage(page);
        vo.setSize(size);
        vo.setTotal(result.getTotal());
        vo.setRecords(records);
        return vo;
    }
}