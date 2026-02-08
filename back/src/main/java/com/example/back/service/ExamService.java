package com.example.back.service;

import com.example.back.dto.ExamCreateRequest;
import com.example.back.dto.ExamSubmitRequest;
import com.example.back.vo.ExamCreateVO;
import com.example.back.vo.ExamSubmitVO;

/**
 * 模拟考试服务
 */
public interface ExamService {

    ExamCreateVO createMockExam(ExamCreateRequest request);

    ExamCreateVO createTaskExam(Long taskId);

    ExamSubmitVO submitMockExam(ExamSubmitRequest request);
}
