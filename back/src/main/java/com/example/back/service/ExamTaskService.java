package com.example.back.service;

import com.example.back.dto.TeacherExamTaskCreateRequest;
import com.example.back.vo.ExamSubmissionVO;
import com.example.back.vo.ExamTaskVO;

import java.util.List;

/**
 * 考试任务服务
 */
public interface ExamTaskService {

    Long createTask(TeacherExamTaskCreateRequest request);

    List<ExamTaskVO> listTeacherTasks();

    void deleteTask(Long taskId);

    List<ExamTaskVO> listStudentTasks();

    List<ExamSubmissionVO> listMySubmissions();
}

