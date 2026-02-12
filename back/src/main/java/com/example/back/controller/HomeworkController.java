package com.example.back.controller;

import com.example.back.common.ApiResponse;
import com.example.back.service.HomeworkService;
import com.example.back.vo.HomeworkDetailVO;
import com.example.back.vo.HomeworkItemVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生作业接口
 */
@RestController
@RequestMapping("/api/homework")
public class HomeworkController {

    private final HomeworkService homeworkService;

    public HomeworkController(HomeworkService homeworkService) {
        this.homeworkService = homeworkService;
    }

    @GetMapping
    public ApiResponse<List<HomeworkItemVO>> list(@RequestParam(value = "courseId", required = false) Long courseId) {
        return ApiResponse.ok(homeworkService.listMyHomework(courseId));
    }

    @GetMapping("/{id}")
    public ApiResponse<HomeworkDetailVO> detail(@PathVariable("id") Long id) {
        return ApiResponse.ok(homeworkService.myHomeworkDetail(id));
    }
}

