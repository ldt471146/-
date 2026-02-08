package com.example.back.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.back.dto.TeacherExamTaskCreateRequest;
import com.example.back.entity.EduChapter;
import com.example.back.entity.EduCourse;
import com.example.back.entity.EduCourseEnroll;
import com.example.back.entity.EduExamSubmission;
import com.example.back.entity.EduExamTask;
import com.example.back.entity.EduExamTaskQuestion;
import com.example.back.entity.EduQuestion;
import com.example.back.mapper.EduChapterMapper;
import com.example.back.mapper.EduCourseEnrollMapper;
import com.example.back.mapper.EduCourseMapper;
import com.example.back.mapper.EduExamSubmissionMapper;
import com.example.back.mapper.EduExamTaskMapper;
import com.example.back.mapper.EduExamTaskQuestionMapper;
import com.example.back.mapper.EduQuestionMapper;
import com.example.back.service.ExamTaskService;
import com.example.back.util.SecurityUtil;
import com.example.back.vo.ExamSubmissionVO;
import com.example.back.vo.ExamTaskVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 考试任务服务实现
 */
@Service
public class ExamTaskServiceImpl implements ExamTaskService {

    private final EduExamTaskMapper taskMapper;
    private final EduExamTaskQuestionMapper taskQuestionMapper;
    private final EduExamSubmissionMapper submissionMapper;
    private final EduCourseMapper courseMapper;
    private final EduChapterMapper chapterMapper;
    private final EduQuestionMapper questionMapper;
    private final EduCourseEnrollMapper enrollMapper;

    public ExamTaskServiceImpl(EduExamTaskMapper taskMapper,
                               EduExamTaskQuestionMapper taskQuestionMapper,
                               EduExamSubmissionMapper submissionMapper,
                               EduCourseMapper courseMapper,
                               EduChapterMapper chapterMapper,
                               EduQuestionMapper questionMapper,
                               EduCourseEnrollMapper enrollMapper) {
        this.taskMapper = taskMapper;
        this.taskQuestionMapper = taskQuestionMapper;
        this.submissionMapper = submissionMapper;
        this.courseMapper = courseMapper;
        this.chapterMapper = chapterMapper;
        this.questionMapper = questionMapper;
        this.enrollMapper = enrollMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTask(TeacherExamTaskCreateRequest request) {
        Long userId = requireUserId();
        EduCourse course = courseMapper.selectById(request.getCourseId());
        if (course == null) {
            throw new IllegalArgumentException("课程不存在");
        }
        if (!userId.equals(course.getTeacherId())) {
            throw new IllegalArgumentException("无权限创建该课程考试");
        }
        if (request.getChapterId() != null) {
            EduChapter chapter = chapterMapper.selectById(request.getChapterId());
            if (chapter == null || !request.getCourseId().equals(chapter.getCourseId())) {
                throw new IllegalArgumentException("章节不属于该课程");
            }
        }
        if (request.getEndTime() != null && request.getStartTime() != null &&
                !request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("结束时间必须晚于开始时间");
        }

        List<EduQuestion> pool = questionMapper.selectList(new LambdaQueryWrapper<EduQuestion>()
                .eq(EduQuestion::getCourseId, request.getCourseId())
                .eq(request.getChapterId() != null, EduQuestion::getChapterId, request.getChapterId()));
        if (pool.isEmpty()) {
            throw new IllegalArgumentException("该课程暂无题目，无法创建考试");
        }

        int count = request.getQuestionCount() == null ? 10 : request.getQuestionCount();
        if (pool.size() < count) {
            throw new IllegalArgumentException("题库题目不足，当前仅 " + pool.size() + " 题");
        }
        Collections.shuffle(pool);
        List<EduQuestion> selected = pool.subList(0, count);

        EduExamTask task = new EduExamTask();
        task.setTeacherId(userId);
        task.setCourseId(request.getCourseId());
        task.setChapterId(request.getChapterId());
        task.setTitle(request.getTitle());
        task.setQuestionCount(count);
        task.setDurationMinutes(request.getDurationMinutes() == null ? 30 : request.getDurationMinutes());
        task.setStartTime(request.getStartTime() == null ? LocalDateTime.now() : request.getStartTime());
        task.setEndTime(request.getEndTime());
        task.setStatus(1);
        taskMapper.insert(task);

        for (EduQuestion q : selected) {
            EduExamTaskQuestion relation = new EduExamTaskQuestion();
            relation.setTaskId(task.getId());
            relation.setQuestionId(q.getId());
            taskQuestionMapper.insert(relation);
        }
        return task.getId();
    }

    @Override
    public List<ExamTaskVO> listTeacherTasks() {
        Long userId = requireUserId();
        List<EduExamTask> tasks = taskMapper.selectList(new LambdaQueryWrapper<EduExamTask>()
                .eq(EduExamTask::getTeacherId, userId)
                .orderByDesc(EduExamTask::getId));
        return buildTaskVOs(tasks, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(Long taskId) {
        Long userId = requireUserId();
        EduExamTask task = taskMapper.selectById(taskId);
        if (task == null) {
            return;
        }
        if (!userId.equals(task.getTeacherId())) {
            throw new IllegalArgumentException("无权限删除该考试");
        }
        taskQuestionMapper.delete(new LambdaQueryWrapper<EduExamTaskQuestion>()
                .eq(EduExamTaskQuestion::getTaskId, taskId));
        taskMapper.deleteById(taskId);
    }

    @Override
    public List<ExamTaskVO> listStudentTasks() {
        Long userId = requireUserId();
        List<EduCourseEnroll> enrolls = enrollMapper.selectList(new LambdaQueryWrapper<EduCourseEnroll>()
                .eq(EduCourseEnroll::getUserId, userId)
                .eq(EduCourseEnroll::getStatus, 1));
        if (enrolls.isEmpty()) {
            return List.of();
        }
        Set<Long> courseIds = enrolls.stream().map(EduCourseEnroll::getCourseId).collect(Collectors.toSet());
        List<EduExamTask> tasks = taskMapper.selectList(new LambdaQueryWrapper<EduExamTask>()
                .in(EduExamTask::getCourseId, courseIds)
                .eq(EduExamTask::getStatus, 1)
                .orderByDesc(EduExamTask::getId));
        return buildTaskVOs(tasks, userId);
    }

    @Override
    public List<ExamSubmissionVO> listMySubmissions() {
        Long userId = requireUserId();
        List<EduExamSubmission> submissions = submissionMapper.selectList(new LambdaQueryWrapper<EduExamSubmission>()
                .eq(EduExamSubmission::getUserId, userId)
                .orderByDesc(EduExamSubmission::getId));
        if (submissions.isEmpty()) {
            return List.of();
        }
        Set<Long> taskIds = submissions.stream().map(EduExamSubmission::getTaskId).collect(Collectors.toSet());
        Map<Long, String> taskTitleMap = taskMapper.selectBatchIds(taskIds)
                .stream()
                .collect(Collectors.toMap(EduExamTask::getId, EduExamTask::getTitle));

        List<ExamSubmissionVO> list = new ArrayList<>();
        for (EduExamSubmission submission : submissions) {
            ExamSubmissionVO vo = new ExamSubmissionVO();
            vo.setId(submission.getId());
            vo.setTaskId(submission.getTaskId());
            vo.setTaskTitle(taskTitleMap.getOrDefault(submission.getTaskId(), "-"));
            vo.setScore(submission.getScore());
            vo.setTotalCount(submission.getTotalCount());
            vo.setCorrectCount(submission.getCorrectCount());
            vo.setSubmittedAt(submission.getSubmittedAt());
            list.add(vo);
        }
        return list;
    }

    private List<ExamTaskVO> buildTaskVOs(List<EduExamTask> tasks, Long userId) {
        if (tasks == null || tasks.isEmpty()) {
            return List.of();
        }
        Set<Long> courseIds = tasks.stream().map(EduExamTask::getCourseId).collect(Collectors.toSet());
        Set<Long> chapterIds = tasks.stream().map(EduExamTask::getChapterId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> courseTitleMap = courseMapper.selectBatchIds(courseIds).stream()
                .collect(Collectors.toMap(EduCourse::getId, EduCourse::getTitle));
        Map<Long, String> chapterTitleMap = chapterIds.isEmpty() ? Map.of() :
                chapterMapper.selectBatchIds(chapterIds).stream()
                        .collect(Collectors.toMap(EduChapter::getId, EduChapter::getTitle));

        Map<Long, EduExamSubmission> latestSubmissionMap = Map.of();
        if (userId != null) {
            List<EduExamSubmission> submissions = submissionMapper.selectList(new LambdaQueryWrapper<EduExamSubmission>()
                    .eq(EduExamSubmission::getUserId, userId)
                    .in(EduExamSubmission::getTaskId, tasks.stream().map(EduExamTask::getId).collect(Collectors.toSet()))
                    .orderByDesc(EduExamSubmission::getId));
            latestSubmissionMap = submissions.stream()
                    .collect(Collectors.toMap(EduExamSubmission::getTaskId, s -> s, (a, b) -> a));
        }

        List<ExamTaskVO> list = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (EduExamTask task : tasks) {
            ExamTaskVO vo = new ExamTaskVO();
            vo.setId(task.getId());
            vo.setTitle(task.getTitle());
            vo.setCourseId(task.getCourseId());
            vo.setCourseTitle(courseTitleMap.getOrDefault(task.getCourseId(), "-"));
            vo.setChapterId(task.getChapterId());
            vo.setChapterTitle(task.getChapterId() == null ? "全课程" : chapterTitleMap.getOrDefault(task.getChapterId(), "-"));
            vo.setQuestionCount(task.getQuestionCount());
            vo.setDurationMinutes(task.getDurationMinutes());
            vo.setStartTime(task.getStartTime());
            vo.setEndTime(task.getEndTime());
            int status = task.getStatus() == null ? 1 : task.getStatus();
            if (task.getEndTime() != null && now.isAfter(task.getEndTime())) {
                status = 2;
            }
            vo.setStatus(status);
            if (userId != null) {
                EduExamSubmission latest = latestSubmissionMap.get(task.getId());
                vo.setSubmitted(latest != null);
                vo.setLatestScore(latest == null ? null : latest.getScore());
            } else {
                vo.setSubmitted(false);
                vo.setLatestScore(null);
            }
            list.add(vo);
        }
        list.sort(Comparator.comparing(ExamTaskVO::getId).reversed());
        return list;
    }

    private Long requireUserId() {
        Long userId = SecurityUtil.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        return userId;
    }
}

