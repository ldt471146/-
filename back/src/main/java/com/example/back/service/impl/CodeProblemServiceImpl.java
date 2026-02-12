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
import com.example.back.service.LocalJudgeClient;
import com.example.back.util.SecurityUtil;
import com.example.back.vo.CodeProblemDetailVO;
import com.example.back.vo.CodeProblemVO;
import com.example.back.vo.CodeSampleVO;
import com.example.back.vo.CodeSubmitResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 编程题服务实现
 */
@Service
@Slf4j
public class CodeProblemServiceImpl implements CodeProblemService {

    private final EduCodeProblemMapper problemMapper;
    private final EduCodeTestcaseMapper testcaseMapper;
    private final EduCodeSubmissionMapper submissionMapper;
    private final LocalJudgeClient localJudgeClient;

    public CodeProblemServiceImpl(EduCodeProblemMapper problemMapper,
                                  EduCodeTestcaseMapper testcaseMapper,
                                  EduCodeSubmissionMapper submissionMapper,
                                  LocalJudgeClient localJudgeClient) {
        this.problemMapper = problemMapper;
        this.testcaseMapper = testcaseMapper;
        this.submissionMapper = submissionMapper;
        this.localJudgeClient = localJudgeClient;
    }

    @Override
    public List<CodeProblemVO> listProblems(Long courseId, Long chapterId) {
        List<EduCodeProblem> problems = problemMapper.selectList(new LambdaQueryWrapper<EduCodeProblem>()
                .eq(EduCodeProblem::getStatus, 1)
                .eq(courseId != null, EduCodeProblem::getCourseId, courseId)
                .eq(chapterId != null, EduCodeProblem::getChapterId, chapterId)
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
        vo.setCourseId(problem.getCourseId());
        vo.setChapterId(problem.getChapterId());
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
        long startNs = System.nanoTime();
        Long userId = SecurityUtil.getUserId();
        try {
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

            List<LocalJudgeClient.TestCaseInput> judgeCases = new ArrayList<>();
            for (EduCodeTestcase tc : testcases) {
                LocalJudgeClient.TestCaseInput testCaseInput = new LocalJudgeClient.TestCaseInput();
                testCaseInput.setInput(tc.getInputData());
                testCaseInput.setExpectedOutput(tc.getOutputData());
                judgeCases.add(testCaseInput);
            }
            LocalJudgeClient.JudgeOutcome outcome = localJudgeClient.judgeAll(
                    request.getSourceCode(),
                    request.getLanguageId(),
                    judgeCases
            );
            int passed = outcome.getPassed() == null ? 0 : outcome.getPassed();
            int total = outcome.getTotal() == null ? 0 : outcome.getTotal();
            List<String> messages = outcome.getMessages() == null ? List.of() : outcome.getMessages();
            String result = normalizeResult(outcome.getResult());

            EduCodeSubmission submission = new EduCodeSubmission();
            submission.setUserId(userId);
            submission.setProblemId(request.getProblemId());
            submission.setLanguageId(request.getLanguageId());
            submission.setSourceCode(request.getSourceCode());
            submission.setResult(result);
            submission.setPassedCount(passed);
            submission.setTotalCount(total);
            submissionMapper.insert(submission);
            log.info("code submission: userId={}, problemId={}, result={}, passed={}/{}",
                    userId, request.getProblemId(), result, passed, total);

            CodeSubmitResultVO vo = new CodeSubmitResultVO();
            vo.setProblemId(request.getProblemId());
            vo.setResult(result);
            vo.setResultLabel(resultLabel(result));
            vo.setErrorType(errorType(result));
            vo.setFailedCaseIndex(outcome.getFailedCaseIndex());
            vo.setPassed(passed);
            vo.setTotal(total);
            vo.setMessages(messages);
            return vo;
        } finally {
            long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;
            if (elapsedMs > 3000) {
                log.warn("perf.slow codeSubmit userId={}, problemId={}, costMs={}",
                        userId, request.getProblemId(), elapsedMs);
            } else {
                log.info("perf codeSubmit userId={}, problemId={}, costMs={}",
                        userId, request.getProblemId(), elapsedMs);
            }
        }
    }

    private String normalizeResult(String raw) {
        if (raw == null || raw.isBlank()) {
            return LocalJudgeClient.RESULT_IE;
        }
        return raw;
    }

    private String resultLabel(String result) {
        return switch (result) {
            case LocalJudgeClient.RESULT_AC -> "Accepted";
            case LocalJudgeClient.RESULT_WA -> "Wrong Answer";
            case LocalJudgeClient.RESULT_CE -> "Compile Error";
            case LocalJudgeClient.RESULT_RE -> "Runtime Error";
            case LocalJudgeClient.RESULT_TLE -> "Time Limit Exceeded";
            default -> "Internal Error";
        };
    }

    private String errorType(String result) {
        return switch (result) {
            case LocalJudgeClient.RESULT_AC -> "NONE";
            case LocalJudgeClient.RESULT_WA -> "WRONG_ANSWER";
            case LocalJudgeClient.RESULT_CE -> "COMPILE_ERROR";
            case LocalJudgeClient.RESULT_RE -> "RUNTIME_ERROR";
            case LocalJudgeClient.RESULT_TLE -> "TIMEOUT";
            default -> "SYSTEM_ERROR";
        };
    }

    private CodeProblemVO toProblemVO(EduCodeProblem p) {
        CodeProblemVO vo = new CodeProblemVO();
        vo.setId(p.getId());
        vo.setCourseId(p.getCourseId());
        vo.setChapterId(p.getChapterId());
        vo.setTitle(p.getTitle());
        vo.setDifficulty(p.getDifficulty());
        vo.setTimeLimit(p.getTimeLimit());
        vo.setMemoryLimit(p.getMemoryLimit());
        return vo;
    }
}
