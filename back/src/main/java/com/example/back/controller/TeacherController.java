package com.example.back.controller;

import com.example.back.common.ApiResponse;
import com.example.back.dto.TeacherChapterRequest;
import com.example.back.dto.TeacherCourseRequest;
import com.example.back.dto.TeacherQuestionImportRequest;
import com.example.back.dto.TeacherLessonRequest;
import com.example.back.dto.TeacherQuestionRequest;
import com.example.back.service.TeacherService;
import com.example.back.vo.CourseDetailVO;
import com.example.back.vo.PageResultVO;
import com.example.back.vo.QuestionVO;
import com.example.back.vo.TeacherCourseVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教师端接口
 */
@RestController
@RequestMapping("/api/teacher")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping("/courses")
    public ApiResponse<List<TeacherCourseVO>> myCourses() {
        return ApiResponse.ok(teacherService.listMyCourses());
    }

    @PostMapping("/courses")
    public ApiResponse<Long> createCourse(@Valid @RequestBody TeacherCourseRequest request) {
        return ApiResponse.ok(teacherService.createCourse(request));
    }

    @PutMapping("/courses/{id}")
    public ApiResponse<Void> updateCourse(@PathVariable("id") Long id,
                                          @Valid @RequestBody TeacherCourseRequest request) {
        teacherService.updateCourse(id, request);
        return ApiResponse.ok();
    }

    @DeleteMapping("/courses/{id}")
    public ApiResponse<Void> deleteCourse(@PathVariable("id") Long id) {
        teacherService.deleteCourse(id);
        return ApiResponse.ok();
    }

    @GetMapping("/courses/{id}")
    public ApiResponse<CourseDetailVO> courseDetail(@PathVariable("id") Long id) {
        return ApiResponse.ok(teacherService.courseDetail(id));
    }

    @PostMapping("/courses/{id}/chapters")
    public ApiResponse<Long> addChapter(@PathVariable("id") Long courseId,
                                        @Valid @RequestBody TeacherChapterRequest request) {
        return ApiResponse.ok(teacherService.addChapter(courseId, request));
    }

    @PutMapping("/chapters/{id}")
    public ApiResponse<Void> updateChapter(@PathVariable("id") Long id,
                                           @Valid @RequestBody TeacherChapterRequest request) {
        teacherService.updateChapter(id, request);
        return ApiResponse.ok();
    }

    @DeleteMapping("/chapters/{id}")
    public ApiResponse<Void> deleteChapter(@PathVariable("id") Long id) {
        teacherService.deleteChapter(id);
        return ApiResponse.ok();
    }

    @PostMapping("/chapters/{id}/lessons")
    public ApiResponse<Long> addLesson(@PathVariable("id") Long chapterId,
                                       @Valid @RequestBody TeacherLessonRequest request) {
        return ApiResponse.ok(teacherService.addLesson(chapterId, request));
    }

    @PutMapping("/lessons/{id}")
    public ApiResponse<Void> updateLesson(@PathVariable("id") Long id,
                                          @Valid @RequestBody TeacherLessonRequest request) {
        teacherService.updateLesson(id, request);
        return ApiResponse.ok();
    }

    @DeleteMapping("/lessons/{id}")
    public ApiResponse<Void> deleteLesson(@PathVariable("id") Long id) {
        teacherService.deleteLesson(id);
        return ApiResponse.ok();
    }

    @GetMapping("/questions")
    public ApiResponse<PageResultVO<com.example.back.vo.TeacherQuestionVO>> listQuestions(
            @RequestParam("courseId") Long courseId,
            @RequestParam(value = "chapterId", required = false) Long chapterId,
            @RequestParam(value = "page", defaultValue = "1") long page,
            @RequestParam(value = "size", defaultValue = "10") long size) {
        return ApiResponse.ok(teacherService.listQuestions(courseId, chapterId, page, size));
    }

    @PostMapping("/questions")
    public ApiResponse<Long> createQuestion(@Valid @RequestBody TeacherQuestionRequest request) {
        return ApiResponse.ok(teacherService.createQuestion(request));
    }

    @PutMapping("/questions/{id}")
    public ApiResponse<Void> updateQuestion(@PathVariable("id") Long id,
                                            @Valid @RequestBody TeacherQuestionRequest request) {
        teacherService.updateQuestion(id, request);
        return ApiResponse.ok();
    }

    @DeleteMapping("/questions/{id}")
    public ApiResponse<Void> deleteQuestion(@PathVariable("id") Long id) {
        teacherService.deleteQuestion(id);
        return ApiResponse.ok();
    }

    @PostMapping("/questions/import")
    public ApiResponse<Integer> importQuestions(@Valid @RequestBody TeacherQuestionImportRequest request) {
        return ApiResponse.ok(teacherService.importQuestions(request));
    }
}
