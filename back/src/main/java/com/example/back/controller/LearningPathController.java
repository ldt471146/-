package com.example.back.controller;

import com.example.back.common.ApiResponse;
import com.example.back.dto.LearningPathProgressRequest;
import com.example.back.service.LearningPathService;
import com.example.back.vo.LearningPathOverviewVO;
import org.springframework.web.bind.annotation.*;

/**
 * 学习路径
 */
@RestController
@RequestMapping("/api/learning-path")
public class LearningPathController {

    private final LearningPathService learningPathService;

    public LearningPathController(LearningPathService learningPathService) {
        this.learningPathService = learningPathService;
    }

    @GetMapping
    public ApiResponse<LearningPathOverviewVO> path(@RequestParam("courseId") Long courseId) {
        return ApiResponse.ok(learningPathService.path(courseId));
    }

    @PostMapping("/progress")
    public ApiResponse<Void> markProgress(@RequestBody LearningPathProgressRequest request) {
        learningPathService.markProgress(request);
        return ApiResponse.ok();
    }
}

