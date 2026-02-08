package com.example.back.controller;

import com.example.back.common.ApiResponse;
import com.example.back.service.ExamService;
import com.example.back.service.ExamTaskService;
import com.example.back.vo.ExamCreateVO;
import com.example.back.vo.ExamSubmissionVO;
import com.example.back.vo.ExamTaskVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 学生考试任务接口
 */
@RestController
@RequestMapping("/api/exam-tasks")
public class ExamTaskController {

    private final ExamTaskService examTaskService;
    private final ExamService examService;

    public ExamTaskController(ExamTaskService examTaskService, ExamService examService) {
        this.examTaskService = examTaskService;
        this.examService = examService;
    }

    @GetMapping
    public ApiResponse<List<ExamTaskVO>> listStudentTasks() {
        return ApiResponse.ok(examTaskService.listStudentTasks());
    }

    @GetMapping("/my-submissions")
    public ApiResponse<List<ExamSubmissionVO>> mySubmissions() {
        return ApiResponse.ok(examTaskService.listMySubmissions());
    }

    @PostMapping("/{id}/start")
    public ApiResponse<ExamCreateVO> start(@PathVariable("id") Long id) {
        return ApiResponse.ok(examService.createTaskExam(id));
    }
}

