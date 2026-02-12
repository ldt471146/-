package com.example.back.service;

import com.example.back.dto.LearningPathProgressRequest;
import com.example.back.dto.TeacherKnowledgeDependencyRequest;
import com.example.back.dto.TeacherKnowledgePointRequest;
import com.example.back.entity.EduKnowledgePoint;
import com.example.back.vo.LearningPathOverviewVO;

import java.util.List;

public interface LearningPathService {

    List<EduKnowledgePoint> listTeacherPoints(Long courseId);

    Long createTeacherPoint(Long courseId, TeacherKnowledgePointRequest request);

    void updateTeacherPoint(Long pointId, TeacherKnowledgePointRequest request);

    void deleteTeacherPoint(Long pointId);

    Long createDependency(TeacherKnowledgeDependencyRequest request);

    void deleteDependency(Long id);

    LearningPathOverviewVO path(Long courseId);

    void markProgress(LearningPathProgressRequest request);
}

