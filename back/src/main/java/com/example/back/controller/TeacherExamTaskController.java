package com.example.back.controller;

import com.example.back.common.ApiResponse;
import com.example.back.dto.TeacherExamTaskCreateRequest;
import com.example.back.service.ExamTaskService;
import com.example.back.vo.ExamTaskVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教师考试任务接口
 */
@RestController
@RequestMapping("/api/teacher/exams")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherExamTaskController {

    private final ExamTaskService examTaskService;

    public TeacherExamTaskController(ExamTaskService examTaskService) {
        this.examTaskService = examTaskService;
    }

    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody TeacherExamTaskCreateRequest request) {
        return ApiResponse.ok(examTaskService.createTask(request));
    }

    @GetMapping
    public ApiResponse<List<ExamTaskVO>> list() {
        return ApiResponse.ok(examTaskService.listTeacherTasks());
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        examTaskService.deleteTask(id);
        return ApiResponse.ok();
    }
}

