package com.example.back.controller;

import com.example.back.common.ApiResponse;
import com.example.back.dto.QuestionSubmitRequest;
import com.example.back.service.QuestionService;
import com.example.back.vo.PageResultVO;
import com.example.back.vo.QuestionSubmitResultVO;
import com.example.back.vo.QuestionVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 题库接口
 */
@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public ApiResponse<PageResultVO<QuestionVO>> list(
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "chapterId", required = false) Long chapterId,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "difficulty", required = false) Integer difficulty,
            @RequestParam(value = "page", defaultValue = "1") long page,
            @RequestParam(value = "size", defaultValue = "10") long size
    ) {
        return ApiResponse.ok(questionService.listQuestionsPage(courseId, chapterId, type, difficulty, page, size));
    }

    @PostMapping("/submit")
    public ApiResponse<QuestionSubmitResultVO> submit(@RequestBody QuestionSubmitRequest request) {
        return ApiResponse.ok(questionService.submit(request));
    }

    @GetMapping("/wrong")
    public ApiResponse<PageResultVO<QuestionVO>> wrongList(
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "chapterId", required = false) Long chapterId,
            @RequestParam(value = "page", defaultValue = "1") long page,
            @RequestParam(value = "size", defaultValue = "10") long size
    ) {
        return ApiResponse.ok(questionService.listWrongQuestionsPage(courseId, chapterId, page, size));
    }

    @GetMapping("/favorites")
    public ApiResponse<PageResultVO<QuestionVO>> favorites(
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "chapterId", required = false) Long chapterId,
            @RequestParam(value = "page", defaultValue = "1") long page,
            @RequestParam(value = "size", defaultValue = "10") long size
    ) {
        return ApiResponse.ok(questionService.listFavoriteQuestionsPage(courseId, chapterId, page, size));
    }

    @GetMapping("/favorite-ids")
    public ApiResponse<List<Long>> favoriteIds() {
        return ApiResponse.ok(questionService.listFavoriteIds());
    }

    @PostMapping("/{id}/favorite")
    public ApiResponse<Void> favorite(@PathVariable("id") Long id) {
        questionService.favorite(id);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}/favorite")
    public ApiResponse<Void> cancelFavorite(@PathVariable("id") Long id) {
        questionService.cancelFavorite(id);
        return ApiResponse.ok();
    }

    @GetMapping("/stats")
    public ApiResponse<com.example.back.vo.QuestionStatsVO> stats() {
        return ApiResponse.ok(questionService.stats());
    }
}
