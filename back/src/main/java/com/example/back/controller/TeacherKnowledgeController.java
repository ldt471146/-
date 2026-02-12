package com.example.back.controller;

import com.example.back.common.ApiResponse;
import com.example.back.dto.TeacherKnowledgeDependencyRequest;
import com.example.back.dto.TeacherKnowledgePointRequest;
import com.example.back.entity.EduKnowledgePoint;
import com.example.back.service.LearningPathService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教师端知识点维护
 */
@RestController
@RequestMapping("/api/teacher/knowledge")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherKnowledgeController {

    private final LearningPathService learningPathService;

    public TeacherKnowledgeController(LearningPathService learningPathService) {
        this.learningPathService = learningPathService;
    }

    @GetMapping("/courses/{courseId}/points")
    public ApiResponse<List<EduKnowledgePoint>> listPoints(@PathVariable("courseId") Long courseId) {
        return ApiResponse.ok(learningPathService.listTeacherPoints(courseId));
    }

    @PostMapping("/courses/{courseId}/points")
    public ApiResponse<Long> createPoint(@PathVariable("courseId") Long courseId,
                                         @RequestBody TeacherKnowledgePointRequest request) {
        return ApiResponse.ok(learningPathService.createTeacherPoint(courseId, request));
    }

    @PutMapping("/points/{id}")
    public ApiResponse<Void> updatePoint(@PathVariable("id") Long id,
                                         @RequestBody TeacherKnowledgePointRequest request) {
        learningPathService.updateTeacherPoint(id, request);
        return ApiResponse.ok();
    }

    @DeleteMapping("/points/{id}")
    public ApiResponse<Void> deletePoint(@PathVariable("id") Long id) {
        learningPathService.deleteTeacherPoint(id);
        return ApiResponse.ok();
    }

    @PostMapping("/dependencies")
    public ApiResponse<Long> createDependency(@RequestBody TeacherKnowledgeDependencyRequest request) {
        return ApiResponse.ok(learningPathService.createDependency(request));
    }

    @DeleteMapping("/dependencies/{id}")
    public ApiResponse<Void> deleteDependency(@PathVariable("id") Long id) {
        learningPathService.deleteDependency(id);
        return ApiResponse.ok();
    }
}

