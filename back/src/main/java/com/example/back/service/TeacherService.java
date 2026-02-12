package com.example.back.service;

import com.example.back.dto.TeacherChapterRequest;
import com.example.back.dto.TeacherCourseRequest;
import com.example.back.dto.TeacherQuestionImportRequest;
import com.example.back.dto.TeacherLessonRequest;
import com.example.back.dto.TeacherCodeProblemRequest;
import com.example.back.dto.TeacherQuestionRequest;
import com.example.back.vo.CourseDetailVO;
import com.example.back.vo.PageResultVO;
import com.example.back.vo.QuestionVO;
import com.example.back.vo.TeacherCourseVO;

import java.util.List;

/**
 * 教师端服务
 */
public interface TeacherService {

    List<TeacherCourseVO> listMyCourses();

    Long createCourse(TeacherCourseRequest request);

    void updateCourse(Long courseId, TeacherCourseRequest request);

    void deleteCourse(Long courseId);

    CourseDetailVO courseDetail(Long courseId);

    Long addChapter(Long courseId, TeacherChapterRequest request);

    void updateChapter(Long chapterId, TeacherChapterRequest request);

    void deleteChapter(Long chapterId);

    Long addLesson(Long chapterId, TeacherLessonRequest request);

    void updateLesson(Long lessonId, TeacherLessonRequest request);

    void deleteLesson(Long lessonId);

    PageResultVO<com.example.back.vo.TeacherQuestionVO> listQuestions(Long courseId, Long chapterId, long page, long size);

    Long createQuestion(TeacherQuestionRequest request);

    void updateQuestion(Long id, TeacherQuestionRequest request);

    void deleteQuestion(Long id);

    int importQuestions(TeacherQuestionImportRequest request);

    PageResultVO<com.example.back.vo.TeacherCodeProblemVO> listCodeProblems(Long courseId, Long chapterId, long page, long size);

    Long createCodeProblem(TeacherCodeProblemRequest request);

    void updateCodeProblem(Long id, TeacherCodeProblemRequest request);

    void deleteCodeProblem(Long id);
}
