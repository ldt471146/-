package com.example.back.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.back.dto.CodeSubmitRequest;
import com.example.back.entity.EduCodeProblem;
import com.example.back.entity.EduCodeSubmission;
import com.example.back.entity.EduCodeTestcase;
import com.example.back.mapper.EduCodeProblemMapper;
import com.example.back.mapper.EduCodeSubmissionMapper;
import com.example.back.mapper.EduCodeTestcaseMapper;
import com.example.back.service.CodeProblemService;
import com.example.back.service.Judge0Client;
import com.example.back.util.SecurityUtil;
import com.example.back.vo.CodeProblemDetailVO;
import com.example.back.vo.CodeProblemVO;
import com.example.back.vo.CodeSampleVO;
import com.example.back.vo.CodeSubmitResultVO;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 编程题服务实现
 */
@Service
@Profile("judge0")
public class CodeProblemServiceImpl implements CodeProblemService {

    private final EduCodeProblemMapper problemMapper;
    private final EduCodeTestcaseMapper testcaseMapper;
    private final EduCodeSubmissionMapper submissionMapper;
    private final Judge0Client judge0Client;

    public CodeProblemServiceImpl(EduCodeProblemMapper problemMapper,
                                  EduCodeTestcaseMapper testcaseMapper,
                                  EduCodeSubmissionMapper submissionMapper,
                                  Judge0Client judge0Client) {
        this.problemMapper = problemMapper;
        this.testcaseMapper = testcaseMapper;
        this.submissionMapper = submissionMapper;
        this.judge0Client = judge0Client;
    }

    @Override
    public List<CodeProblemVO> listProblems() {
        List<EduCodeProblem> problems = problemMapper.selectList(new LambdaQueryWrapper<EduCodeProblem>()
                .eq(EduCodeProblem::getStatus, 1)
                .orderByDesc(EduCodeProblem::getId));
        return problems.stream().map(this::toProblemVO).collect(Collectors.toList());
    }

    @Override
    public CodeProblemDetailVO getDetail(Long id) {
        EduCodeProblem problem = problemMapper.selectById(id);
        if (problem == null || problem.getStatus() == null || problem.getStatus() != 1) {
            return null;
        }
        List<EduCodeTestcase> samples = testcaseMapper.selectList(new LambdaQueryWrapper<EduCodeTestcase>()
                .eq(EduCodeTestcase::getProblemId, id)
                .eq(EduCodeTestcase::getIsSample, 1)
                .orderByAsc(EduCodeTestcase::getId));

        CodeProblemDetailVO vo = new CodeProblemDetailVO();
        vo.setId(problem.getId());
        vo.setTitle(problem.getTitle());
        vo.setContent(problem.getContent());
        vo.setDifficulty(problem.getDifficulty());
        vo.setTimeLimit(problem.getTimeLimit());
        vo.setMemoryLimit(problem.getMemoryLimit());
        vo.setSamples(samples.stream().map(s -> {
            CodeSampleVO sample = new CodeSampleVO();
            sample.setInput(s.getInputData());
            sample.setOutput(s.getOutputData());
            return sample;
        }).collect(Collectors.toList()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CodeSubmitResultVO submit(CodeSubmitRequest request) {
        Long userId = SecurityUtil.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        EduCodeProblem problem = problemMapper.selectById(request.getProblemId());
        if (problem == null || problem.getStatus() == null || problem.getStatus() != 1) {
            throw new IllegalArgumentException("题目不存在或已下架");
        }
        List<EduCodeTestcase> testcases = testcaseMapper.selectList(new LambdaQueryWrapper<EduCodeTestcase>()
                .eq(EduCodeTestcase::getProblemId, request.getProblemId())
                .orderByAsc(EduCodeTestcase::getId));
        if (testcases.isEmpty()) {
            throw new IllegalArgumentException("题目未配置测试用例");
        }

        int passed = 0;
        List<String> messages = new ArrayList<>();
        for (EduCodeTestcase tc : testcases) {
            JsonNode resp = judge0Client.submit(request.getSourceCode(), request.getLanguageId(),
                    tc.getInputData(), tc.getOutputData());
            int statusId = resp.path("status").path("id").asInt();
            String statusDesc = resp.path("status").path("description").asText();
            if (statusId == 3) {
                passed++;
                messages.add("通过");
            } else {
                messages.add(statusDesc);
            }
        }

        String result = passed == testcases.size() ? "AC" : "WA";
        EduCodeSubmission submission = new EduCodeSubmission();
        submission.setUserId(userId);
        submission.setProblemId(request.getProblemId());
        submission.setLanguageId(request.getLanguageId());
        submission.setSourceCode(request.getSourceCode());
        submission.setResult(result);
        submission.setPassedCount(passed);
        submission.setTotalCount(testcases.size());
        submissionMapper.insert(submission);

        CodeSubmitResultVO vo = new CodeSubmitResultVO();
        vo.setProblemId(request.getProblemId());
        vo.setResult(result);
        vo.setPassed(passed);
        vo.setTotal(testcases.size());
        vo.setMessages(messages);
        return vo;
    }

    private CodeProblemVO toProblemVO(EduCodeProblem p) {
        CodeProblemVO vo = new CodeProblemVO();
        vo.setId(p.getId());
        vo.setTitle(p.getTitle());
        vo.setDifficulty(p.getDifficulty());
        vo.setTimeLimit(p.getTimeLimit());
        vo.setMemoryLimit(p.getMemoryLimit());
        return vo;
    }
}
