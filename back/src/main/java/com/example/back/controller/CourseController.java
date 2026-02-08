package com.example.back.controller;

import com.example.back.common.ApiResponse;
import com.example.back.service.CourseService;
import com.example.back.util.SecurityUtil;
import com.example.back.vo.CourseDetailVO;
import com.example.back.vo.CourseVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 课程接口
 */
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ApiResponse<List<CourseVO>> list() {
        return ApiResponse.ok(courseService.listCourses());
    }

    @GetMapping("/my")
    public ApiResponse<List<CourseVO>> listMy() {
        Long userId = SecurityUtil.getUserId();
        return ApiResponse.ok(courseService.listMyCourses(userId));
    }

    @GetMapping("/{id}")
    public ApiResponse<CourseDetailVO> detail(@PathVariable("id") Long id) {
        return ApiResponse.ok(courseService.getCourseDetail(id));
    }

    @PostMapping("/{id}/enroll")
    public ApiResponse<Void> enroll(@PathVariable("id") Long id) {
        Long userId = SecurityUtil.getUserId();
        courseService.enrollCourse(userId, id);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}/enroll")
    public ApiResponse<Void> cancelEnroll(@PathVariable("id") Long id) {
        Long userId = SecurityUtil.getUserId();
        courseService.cancelEnroll(userId, id);
        return ApiResponse.ok();
    }
}
