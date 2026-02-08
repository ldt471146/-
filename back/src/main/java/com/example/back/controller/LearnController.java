package com.example.back.controller;

import com.example.back.common.ApiResponse;
import com.example.back.dto.LearnProgressRequest;
import com.example.back.service.LearnService;
import com.example.back.vo.LearnRecordVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 学习记录接口
 */
@RestController
@RequestMapping("/api/learn")
public class LearnController {

    private final LearnService learnService;

    public LearnController(LearnService learnService) {
        this.learnService = learnService;
    }

    @PostMapping("/progress")
    public ApiResponse<Void> updateProgress(@Valid @RequestBody LearnProgressRequest request) {
        learnService.updateProgress(request);
        return ApiResponse.ok(null);
    }

    @GetMapping("/records")
    public ApiResponse<List<LearnRecordVO>> records() {
        return ApiResponse.ok(learnService.listMyRecords());
    }
}
