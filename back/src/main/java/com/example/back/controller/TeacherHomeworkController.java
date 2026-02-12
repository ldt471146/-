package com.example.back.controller;

import com.example.back.common.ApiResponse;
import com.example.back.dto.TeacherHomeworkCreateRequest;
import com.example.back.service.HomeworkService;
import com.example.back.vo.HomeworkDetailVO;
import com.example.back.vo.HomeworkItemVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教师作业接口
 */
@RestController
@RequestMapping("/api/teacher/homework")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherHomeworkController {

    private final HomeworkService homeworkService;

    public TeacherHomeworkController(HomeworkService homeworkService) {
        this.homeworkService = homeworkService;
    }

    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody TeacherHomeworkCreateRequest request) {
        return ApiResponse.ok(homeworkService.createByTeacher(request));
    }

    @GetMapping
    public ApiResponse<List<HomeworkItemVO>> list(@RequestParam(value = "courseId", required = false) Long courseId) {
        return ApiResponse.ok(homeworkService.listTeacherHomework(courseId));
    }

    @GetMapping("/{id}")
    public ApiResponse<HomeworkDetailVO> detail(@PathVariable("id") Long id) {
        return ApiResponse.ok(homeworkService.teacherDetail(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        homeworkService.deleteByTeacher(id);
        return ApiResponse.ok();
    }
}

