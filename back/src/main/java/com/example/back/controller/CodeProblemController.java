package com.example.back.controller;

import com.example.back.common.ApiResponse;
import com.example.back.dto.CodeSubmitRequest;
import com.example.back.service.CodeProblemService;
import com.example.back.vo.CodeProblemDetailVO;
import com.example.back.vo.CodeProblemVO;
import com.example.back.vo.CodeSubmitResultVO;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 编程题接口
 */
@RestController
@RequestMapping("/api/code")
@Profile("judge0")
public class CodeProblemController {

    private final CodeProblemService codeProblemService;

    public CodeProblemController(CodeProblemService codeProblemService) {
        this.codeProblemService = codeProblemService;
    }

    @GetMapping("/problems")
    public ApiResponse<List<CodeProblemVO>> list() {
        return ApiResponse.ok(codeProblemService.listProblems());
    }

    @GetMapping("/problems/{id}")
    public ApiResponse<CodeProblemDetailVO> detail(@PathVariable("id") Long id) {
        return ApiResponse.ok(codeProblemService.getDetail(id));
    }

    @PostMapping("/submit")
    public ApiResponse<CodeSubmitResultVO> submit(@RequestBody CodeSubmitRequest request) {
        return ApiResponse.ok(codeProblemService.submit(request));
    }
}
