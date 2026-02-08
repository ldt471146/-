package com.example.back.controller;

import com.example.back.common.ApiResponse;
import com.example.back.dto.ExamCreateRequest;
import com.example.back.dto.ExamSubmitRequest;
import com.example.back.service.ExamService;
import com.example.back.vo.ExamCreateVO;
import com.example.back.vo.ExamSubmitVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 模拟考试接口
 */
@RestController
@RequestMapping("/api/exams")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @PostMapping("/mock")
    public ApiResponse<ExamCreateVO> createMock(@Valid @RequestBody ExamCreateRequest request) {
        return ApiResponse.ok(examService.createMockExam(request));
    }

    @PostMapping("/submit")
    public ApiResponse<ExamSubmitVO> submit(@Valid @RequestBody ExamSubmitRequest request) {
        return ApiResponse.ok(examService.submitMockExam(request));
    }
}

