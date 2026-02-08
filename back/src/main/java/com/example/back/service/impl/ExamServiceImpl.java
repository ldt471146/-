package com.example.back.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.back.dto.ExamAnswerItemRequest;
import com.example.back.dto.ExamCreateRequest;
import com.example.back.dto.ExamSubmitRequest;
import com.example.back.entity.EduQuestion;
import com.example.back.entity.EduQuestionOption;
import com.example.back.entity.EduQuestionRecord;
import com.example.back.entity.EduWrongQuestion;
import com.example.back.entity.EduExamTask;
import com.example.back.entity.EduExamTaskQuestion;
import com.example.back.entity.EduExamSubmission;
import com.example.back.mapper.EduExamTaskMapper;
import com.example.back.mapper.EduExamTaskQuestionMapper;
import com.example.back.mapper.EduExamSubmissionMapper;
import com.example.back.mapper.EduQuestionMapper;
import com.example.back.mapper.EduQuestionOptionMapper;
import com.example.back.mapper.EduQuestionRecordMapper;
import com.example.back.mapper.EduWrongQuestionMapper;
import com.example.back.service.ExamService;
import com.example.back.util.SecurityUtil;
import com.example.back.vo.ExamCreateVO;
import com.example.back.vo.ExamQuestionResultVO;
import com.example.back.vo.ExamQuestionVO;
import com.example.back.vo.ExamSubmitVO;
import com.example.back.vo.QuestionOptionVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 模拟考试服务实现
 */
@Service
public class ExamServiceImpl implements ExamService {

    private static final String EXAM_KEY_PREFIX = "exam:mock:";
    private static final Map<String, LocalCacheEntry> LOCAL_CACHE = new ConcurrentHashMap<>();

    private final EduQuestionMapper questionMapper;
    private final EduQuestionOptionMapper optionMapper;
    private final EduQuestionRecordMapper recordMapper;
    private final EduWrongQuestionMapper wrongMapper;
    private final EduExamTaskMapper taskMapper;
    private final EduExamTaskQuestionMapper taskQuestionMapper;
    private final EduExamSubmissionMapper submissionMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public ExamServiceImpl(EduQuestionMapper questionMapper,
                           EduQuestionOptionMapper optionMapper,
                           EduQuestionRecordMapper recordMapper,
                           EduWrongQuestionMapper wrongMapper,
                           EduExamTaskMapper taskMapper,
                           EduExamTaskQuestionMapper taskQuestionMapper,
                           EduExamSubmissionMapper submissionMapper,
                           StringRedisTemplate redisTemplate,
                           ObjectMapper objectMapper) {
        this.questionMapper = questionMapper;
        this.optionMapper = optionMapper;
        this.recordMapper = recordMapper;
        this.wrongMapper = wrongMapper;
        this.taskMapper = taskMapper;
        this.taskQuestionMapper = taskQuestionMapper;
        this.submissionMapper = submissionMapper;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public ExamCreateVO createMockExam(ExamCreateRequest request) {
        Long userId = requireUserId();
        Integer count = request.getQuestionCount() == null ? 10 : request.getQuestionCount();
        Integer duration = request.getDurationMinutes() == null ? 30 : request.getDurationMinutes();

        List<EduQuestion> pool = questionMapper.selectList(new LambdaQueryWrapper<EduQuestion>()
                .eq(EduQuestion::getCourseId, request.getCourseId())
                .eq(request.getChapterId() != null, EduQuestion::getChapterId, request.getChapterId()));
        if (pool == null || pool.isEmpty()) {
            throw new IllegalArgumentException("该条件下暂无题目，请先录入题库");
        }

        Collections.shuffle(pool);
        int finalCount = Math.min(count, pool.size());
        List<EduQuestion> selected = new ArrayList<>(pool.subList(0, finalCount));
        List<Long> questionIds = selected.stream().map(EduQuestion::getId).collect(Collectors.toList());

        List<EduQuestionOption> options = optionMapper.selectList(new LambdaQueryWrapper<EduQuestionOption>()
                .in(EduQuestionOption::getQuestionId, questionIds)
                .orderByAsc(EduQuestionOption::getLabel));
        Map<Long, List<EduQuestionOption>> optionMap = options.stream()
                .collect(Collectors.groupingBy(EduQuestionOption::getQuestionId));

        List<ExamQuestionVO> questions = selected.stream()
                .map(q -> toExamQuestionVO(q, optionMap.getOrDefault(q.getId(), List.of())))
                .collect(Collectors.toList());

        MockExamSnapshot snapshot = buildSnapshot(userId, duration, selected, optionMap);
        saveSnapshot(snapshot);

        ExamCreateVO vo = new ExamCreateVO();
        vo.setExamId(snapshot.getExamId());
        vo.setDurationMinutes(duration);
        vo.setTotal(finalCount);
        vo.setQuestions(questions);
        return vo;
    }

    @Override
    public ExamCreateVO createTaskExam(Long taskId) {
        Long userId = requireUserId();
        EduExamTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("考试任务不存在");
        }
        if (task.getStatus() == null || task.getStatus() != 1) {
            throw new IllegalArgumentException("考试任务未发布");
        }
        LocalDateTime now = LocalDateTime.now();
        if (task.getStartTime() != null && now.isBefore(task.getStartTime())) {
            throw new IllegalArgumentException("考试未开始");
        }
        if (task.getEndTime() != null && now.isAfter(task.getEndTime())) {
            throw new IllegalArgumentException("考试已结束");
        }

        List<EduExamTaskQuestion> relations = taskQuestionMapper.selectList(new LambdaQueryWrapper<EduExamTaskQuestion>()
                .eq(EduExamTaskQuestion::getTaskId, taskId)
                .orderByAsc(EduExamTaskQuestion::getId));
        if (relations.isEmpty()) {
            throw new IllegalArgumentException("考试题目为空");
        }
        List<Long> questionIds = relations.stream().map(EduExamTaskQuestion::getQuestionId).collect(Collectors.toList());
        List<EduQuestion> questions = questionMapper.selectList(new LambdaQueryWrapper<EduQuestion>()
                .in(EduQuestion::getId, questionIds));
        Map<Long, EduQuestion> questionMap = questions.stream()
                .collect(Collectors.toMap(EduQuestion::getId, v -> v));
        List<EduQuestion> ordered = questionIds.stream()
                .map(questionMap::get)
                .filter(v -> v != null)
                .collect(Collectors.toList());
        if (ordered.isEmpty()) {
            throw new IllegalArgumentException("考试题目不存在");
        }

        List<EduQuestionOption> options = optionMapper.selectList(new LambdaQueryWrapper<EduQuestionOption>()
                .in(EduQuestionOption::getQuestionId, questionIds)
                .orderByAsc(EduQuestionOption::getLabel));
        Map<Long, List<EduQuestionOption>> optionMap = options.stream()
                .collect(Collectors.groupingBy(EduQuestionOption::getQuestionId));

        List<ExamQuestionVO> questionVOs = ordered.stream()
                .map(q -> toExamQuestionVO(q, optionMap.getOrDefault(q.getId(), List.of())))
                .collect(Collectors.toList());

        MockExamSnapshot snapshot = buildSnapshot(userId, task.getDurationMinutes(), ordered, optionMap);
        snapshot.setTaskId(taskId);
        saveSnapshot(snapshot);

        ExamCreateVO vo = new ExamCreateVO();
        vo.setExamId(snapshot.getExamId());
        vo.setDurationMinutes(task.getDurationMinutes());
        vo.setTotal(questionVOs.size());
        vo.setQuestions(questionVOs);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExamSubmitVO submitMockExam(ExamSubmitRequest request) {
        Long userId = requireUserId();
        MockExamSnapshot snapshot = getSnapshot(request.getExamId());
        if (snapshot == null) {
            throw new IllegalArgumentException("考试不存在或已过期，请重新开始");
        }
        if (!userId.equals(snapshot.getUserId())) {
            throw new IllegalArgumentException("无权限提交该考试");
        }

        Map<Long, List<String>> userAnswerMap = new HashMap<>();
        if (request.getAnswers() != null) {
            for (ExamAnswerItemRequest item : request.getAnswers()) {
                if (item == null || item.getQuestionId() == null) {
                    continue;
                }
                userAnswerMap.put(item.getQuestionId(), normalizeAnswers(item.getAnswers()));
            }
        }

        int correctCount = 0;
        List<ExamQuestionResultVO> resultList = new ArrayList<>();
        for (Long questionId : snapshot.getQuestionIds()) {
            List<String> userAnswers = userAnswerMap.getOrDefault(questionId, List.of());
            List<String> correctAnswers = snapshot.getCorrectAnswerMap().getOrDefault(questionId, List.of());
            boolean correct = correctAnswers.equals(userAnswers);
            if (correct) {
                correctCount++;
            }
            persistRecord(userId, questionId, userAnswers, correct);

            ExamQuestionResultVO item = new ExamQuestionResultVO();
            item.setQuestionId(questionId);
            item.setTitle(snapshot.getTitleMap().getOrDefault(questionId, "-"));
            item.setCorrect(correct);
            item.setUserAnswers(userAnswers);
            item.setCorrectAnswers(correctAnswers);
            item.setAnalysis(snapshot.getAnalysisMap().getOrDefault(questionId, ""));
            resultList.add(item);
        }

        int total = snapshot.getQuestionIds().size();
        int score = total == 0 ? 0 : (int) Math.round(correctCount * 100.0 / total);

        if (snapshot.getTaskId() != null) {
            EduExamSubmission submission = new EduExamSubmission();
            submission.setTaskId(snapshot.getTaskId());
            submission.setUserId(userId);
            submission.setTotalCount(total);
            submission.setCorrectCount(correctCount);
            submission.setScore(score);
            submission.setSubmittedAt(LocalDateTime.now());
            submission.setDetailJson(toJson(resultList));
            submissionMapper.insert(submission);
        }

        removeSnapshot(snapshot.getExamId());

        ExamSubmitVO vo = new ExamSubmitVO();
        vo.setTotal(total);
        vo.setCorrectCount(correctCount);
        vo.setWrongCount(total - correctCount);
        vo.setScore(score);
        vo.setResults(resultList);
        return vo;
    }

    private ExamQuestionVO toExamQuestionVO(EduQuestion question, List<EduQuestionOption> options) {
        ExamQuestionVO vo = new ExamQuestionVO();
        vo.setId(question.getId());
        vo.setTitle(question.getTitle());
        vo.setType(question.getType());
        vo.setDifficulty(question.getDifficulty());
        vo.setOptions(options.stream().map(opt -> {
            QuestionOptionVO optionVO = new QuestionOptionVO();
            optionVO.setLabel(opt.getLabel());
            optionVO.setContent(opt.getContent());
            return optionVO;
        }).collect(Collectors.toList()));
        return vo;
    }

    private MockExamSnapshot buildSnapshot(Long userId,
                                           Integer durationMinutes,
                                           List<EduQuestion> selected,
                                           Map<Long, List<EduQuestionOption>> optionMap) {
        MockExamSnapshot snapshot = new MockExamSnapshot();
        snapshot.setExamId(UUID.randomUUID().toString().replace("-", ""));
        snapshot.setUserId(userId);
        snapshot.setDurationMinutes(durationMinutes);
        snapshot.setCreatedAt(LocalDateTime.now());

        List<Long> questionIds = selected.stream().map(EduQuestion::getId).collect(Collectors.toList());
        snapshot.setQuestionIds(questionIds);

        Map<Long, List<String>> correctAnswerMap = new LinkedHashMap<>();
        Map<Long, String> titleMap = new LinkedHashMap<>();
        Map<Long, String> analysisMap = new LinkedHashMap<>();
        for (EduQuestion question : selected) {
            List<String> correct = optionMap.getOrDefault(question.getId(), List.of())
                    .stream()
                    .filter(o -> o.getIsCorrect() != null && o.getIsCorrect() == 1)
                    .map(EduQuestionOption::getLabel)
                    .sorted()
                    .collect(Collectors.toList());
            correctAnswerMap.put(question.getId(), correct);
            titleMap.put(question.getId(), question.getTitle());
            analysisMap.put(question.getId(), question.getAnalysis());
        }
        snapshot.setCorrectAnswerMap(correctAnswerMap);
        snapshot.setTitleMap(titleMap);
        snapshot.setAnalysisMap(analysisMap);
        return snapshot;
    }

    private List<String> normalizeAnswers(List<String> answers) {
        if (answers == null || answers.isEmpty()) {
            return List.of();
        }
        return answers.stream()
                .filter(v -> v != null && !v.isBlank())
                .map(String::trim)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
    }

    private void persistRecord(Long userId, Long questionId, List<String> answers, boolean correct) {
        EduQuestionRecord record = new EduQuestionRecord();
        record.setUserId(userId);
        record.setQuestionId(questionId);
        record.setAnswer(String.join(",", answers));
        record.setIsCorrect(correct ? 1 : 0);
        recordMapper.insert(record);

        if (correct) {
            wrongMapper.delete(new LambdaQueryWrapper<EduWrongQuestion>()
                    .eq(EduWrongQuestion::getUserId, userId)
                    .eq(EduWrongQuestion::getQuestionId, questionId));
            return;
        }

        EduWrongQuestion wrong = wrongMapper.selectOne(new LambdaQueryWrapper<EduWrongQuestion>()
                .eq(EduWrongQuestion::getUserId, userId)
                .eq(EduWrongQuestion::getQuestionId, questionId));
        if (wrong == null) {
            wrong = new EduWrongQuestion();
            wrong.setUserId(userId);
            wrong.setQuestionId(questionId);
            wrong.setWrongCount(1);
            wrongMapper.insert(wrong);
        } else {
            wrong.setWrongCount((wrong.getWrongCount() == null ? 0 : wrong.getWrongCount()) + 1);
            wrongMapper.updateById(wrong);
        }
    }

    private Long requireUserId() {
        Long userId = SecurityUtil.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        return userId;
    }

    private void saveSnapshot(MockExamSnapshot snapshot) {
        String json = toJson(snapshot);
        String key = EXAM_KEY_PREFIX + snapshot.getExamId();
        long seconds = Math.max(300, snapshot.getDurationMinutes() * 60L + 300);
        try {
            redisTemplate.opsForValue().set(key, json, Duration.ofSeconds(seconds));
        } catch (DataAccessException e) {
            LocalCacheEntry entry = new LocalCacheEntry();
            entry.setJson(json);
            entry.setExpireAt(System.currentTimeMillis() + seconds * 1000);
            LOCAL_CACHE.put(snapshot.getExamId(), entry);
        }
    }

    private MockExamSnapshot getSnapshot(String examId) {
        String key = EXAM_KEY_PREFIX + examId;
        String json;
        try {
            json = redisTemplate.opsForValue().get(key);
        } catch (DataAccessException e) {
            LocalCacheEntry entry = LOCAL_CACHE.get(examId);
            if (entry == null || entry.getExpireAt() < System.currentTimeMillis()) {
                LOCAL_CACHE.remove(examId);
                return null;
            }
            json = entry.getJson();
        }
        if (json == null || json.isBlank()) {
            return null;
        }
        return fromJson(json);
    }

    private void removeSnapshot(String examId) {
        String key = EXAM_KEY_PREFIX + examId;
        try {
            redisTemplate.delete(key);
        } catch (DataAccessException e) {
            LOCAL_CACHE.remove(examId);
        }
    }

    private String toJson(MockExamSnapshot snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("考试缓存序列化失败");
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private MockExamSnapshot fromJson(String json) {
        try {
            return objectMapper.readValue(json, MockExamSnapshot.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("考试缓存读取失败");
        }
    }

    private static class LocalCacheEntry {
        private String json;
        private long expireAt;

        public String getJson() {
            return json;
        }

        public void setJson(String json) {
            this.json = json;
        }

        public long getExpireAt() {
            return expireAt;
        }

        public void setExpireAt(long expireAt) {
            this.expireAt = expireAt;
        }
    }

    private static class MockExamSnapshot {
        private String examId;
        private Long userId;
        private Integer durationMinutes;
        private LocalDateTime createdAt;
        private List<Long> questionIds;
        private Long taskId;
        private Map<Long, List<String>> correctAnswerMap;
        private Map<Long, String> titleMap;
        private Map<Long, String> analysisMap;

        public String getExamId() {
            return examId;
        }

        public void setExamId(String examId) {
            this.examId = examId;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Integer getDurationMinutes() {
            return durationMinutes;
        }

        public void setDurationMinutes(Integer durationMinutes) {
            this.durationMinutes = durationMinutes;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public List<Long> getQuestionIds() {
            return questionIds;
        }

        public void setQuestionIds(List<Long> questionIds) {
            this.questionIds = questionIds;
        }

        public Long getTaskId() {
            return taskId;
        }

        public void setTaskId(Long taskId) {
            this.taskId = taskId;
        }

        public Map<Long, List<String>> getCorrectAnswerMap() {
            return correctAnswerMap;
        }

        public void setCorrectAnswerMap(Map<Long, List<String>> correctAnswerMap) {
            this.correctAnswerMap = correctAnswerMap;
        }

        public Map<Long, String> getTitleMap() {
            return titleMap;
        }

        public void setTitleMap(Map<Long, String> titleMap) {
            this.titleMap = titleMap;
        }

        public Map<Long, String> getAnalysisMap() {
            return analysisMap;
        }

        public void setAnalysisMap(Map<Long, String> analysisMap) {
            this.analysisMap = analysisMap;
        }
    }
}
