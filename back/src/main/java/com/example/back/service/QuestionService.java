package com.example.back.service;

import com.example.back.dto.QuestionSubmitRequest;
import com.example.back.vo.QuestionSubmitResultVO;
import com.example.back.vo.QuestionVO;

import java.util.List;

/**
 * 题库服务
 */
public interface QuestionService {

    List<QuestionVO> listQuestions(Long courseId);

    com.example.back.vo.PageResultVO<QuestionVO> listQuestionsPage(Long courseId, Long chapterId, String type, Integer difficulty, long page, long size);

    QuestionSubmitResultVO submit(QuestionSubmitRequest request);

    com.example.back.vo.PageResultVO<QuestionVO> listWrongQuestionsPage(Long courseId, Long chapterId, long page, long size);

    com.example.back.vo.PageResultVO<QuestionVO> listFavoriteQuestionsPage(Long courseId, Long chapterId, long page, long size);

    java.util.List<Long> listFavoriteIds();

    void favorite(Long questionId);

    void cancelFavorite(Long questionId);

    com.example.back.vo.QuestionStatsVO stats();
}
