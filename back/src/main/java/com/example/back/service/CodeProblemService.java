package com.example.back.service;

import com.example.back.dto.CodeSubmitRequest;
import com.example.back.vo.CodeProblemDetailVO;
import com.example.back.vo.CodeProblemVO;
import com.example.back.vo.CodeSubmitResultVO;

import java.util.List;

/**
 * 编程题服务
 */
public interface CodeProblemService {
    List<CodeProblemVO> listProblems(Long courseId, Long chapterId);

    CodeProblemDetailVO getDetail(Long id);

    CodeSubmitResultVO submit(CodeSubmitRequest request);
}
