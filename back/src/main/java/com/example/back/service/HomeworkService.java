package com.example.back.service;

import com.example.back.dto.TeacherHomeworkCreateRequest;
import com.example.back.vo.HomeworkDetailVO;
import com.example.back.vo.HomeworkItemVO;
import com.example.back.vo.HomeworkStatsVO;

import java.util.List;

/**
 * 作业服务
 */
public interface HomeworkService {

    Long createByTeacher(TeacherHomeworkCreateRequest request);

    void updateByTeacher(Long homeworkId, TeacherHomeworkCreateRequest request);

    List<HomeworkItemVO> listTeacherHomework(Long courseId);

    HomeworkDetailVO teacherDetail(Long homeworkId);

    HomeworkStatsVO teacherStats(Long homeworkId);

    void deleteByTeacher(Long homeworkId);

    List<HomeworkItemVO> listMyHomework(Long courseId);

    HomeworkDetailVO myHomeworkDetail(Long homeworkId);
}
