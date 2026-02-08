package com.example.back.service;

import com.example.back.vo.CourseDetailVO;
import com.example.back.vo.CourseVO;

import java.util.List;

/**
 * 课程服务
 */
public interface CourseService {

    List<CourseVO> listCourses();

    CourseDetailVO getCourseDetail(Long courseId);

    List<CourseVO> listMyCourses(Long userId);

    void enrollCourse(Long userId, Long courseId);

    void cancelEnroll(Long userId, Long courseId);
}
