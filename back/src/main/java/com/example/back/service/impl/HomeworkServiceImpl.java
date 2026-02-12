package com.example.back.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.back.dto.TeacherHomeworkCreateRequest;
import com.example.back.dto.TeacherHomeworkProblemRequest;
import com.example.back.entity.EduCourse;
import com.example.back.entity.EduCourseEnroll;
import com.example.back.entity.EduHomework;
import com.example.back.entity.EduHomeworkProblem;
import com.example.back.entity.EduQuestion;
import com.example.back.mapper.EduCourseEnrollMapper;
import com.example.back.mapper.EduCourseMapper;
import com.example.back.mapper.EduHomeworkMapper;
import com.example.back.mapper.EduHomeworkProblemMapper;
import com.example.back.mapper.EduQuestionMapper;
import com.example.back.service.HomeworkService;
import com.example.back.util.SecurityUtil;
import com.example.back.vo.HomeworkDetailVO;
import com.example.back.vo.HomeworkItemVO;
import com.example.back.vo.HomeworkProblemVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 作业服务实现
 */
@Service
public class HomeworkServiceImpl implements HomeworkService {

    private final EduCourseMapper courseMapper;
    private final EduCourseEnrollMapper enrollMapper;
    private final EduQuestionMapper questionMapper;
    private final EduHomeworkMapper homeworkMapper;
    private final EduHomeworkProblemMapper homeworkProblemMapper;

    public HomeworkServiceImpl(EduCourseMapper courseMapper,
                               EduCourseEnrollMapper enrollMapper,
                               EduQuestionMapper questionMapper,
                               EduHomeworkMapper homeworkMapper,
                               EduHomeworkProblemMapper homeworkProblemMapper) {
        this.courseMapper = courseMapper;
        this.enrollMapper = enrollMapper;
        this.questionMapper = questionMapper;
        this.homeworkMapper = homeworkMapper;
        this.homeworkProblemMapper = homeworkProblemMapper;
    }

    private Long requireUserId() {
        Long userId = SecurityUtil.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        return userId;
    }

    private EduCourse requireTeacherCourse(Long courseId, Long teacherId) {
        EduCourse course = courseMapper.selectById(courseId);
        if (course == null || !teacherId.equals(course.getTeacherId())) {
            throw new IllegalArgumentException("课程不存在或无权限");
        }
        return course;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createByTeacher(TeacherHomeworkCreateRequest request) {
        Long teacherId = requireUserId();
        requireTeacherCourse(request.getCourseId(), teacherId);

        EduHomework homework = new EduHomework();
        homework.setCourseId(request.getCourseId());
        homework.setTitle(request.getTitle());
        homework.setDeadline(request.getDeadline());
        homeworkMapper.insert(homework);

        saveHomeworkProblems(homework.getId(), request.getCourseId(), request.getProblems());
        return homework.getId();
    }

    @Override
    public List<HomeworkItemVO> listTeacherHomework(Long courseId) {
        Long teacherId = requireUserId();
        List<EduCourse> teacherCourses = courseMapper.selectList(new LambdaQueryWrapper<EduCourse>()
                .eq(EduCourse::getTeacherId, teacherId));
        if (teacherCourses.isEmpty()) {
            return List.of();
        }

        Map<Long, String> courseTitleMap = teacherCourses.stream()
                .collect(Collectors.toMap(EduCourse::getId, EduCourse::getTitle));
        Set<Long> courseIds = new HashSet<>(courseTitleMap.keySet());

        if (courseId != null) {
            if (!courseIds.contains(courseId)) {
                throw new IllegalArgumentException("课程不存在或无权限");
            }
            courseIds = Set.of(courseId);
        }

        List<EduHomework> homeworkList = homeworkMapper.selectList(new LambdaQueryWrapper<EduHomework>()
                .in(EduHomework::getCourseId, courseIds)
                .orderByDesc(EduHomework::getId));
        return toHomeworkItems(homeworkList, courseTitleMap);
    }

    @Override
    public HomeworkDetailVO teacherDetail(Long homeworkId) {
        Long teacherId = requireUserId();
        EduHomework homework = homeworkMapper.selectById(homeworkId);
        if (homework == null) {
            throw new IllegalArgumentException("作业不存在");
        }
        EduCourse course = requireTeacherCourse(homework.getCourseId(), teacherId);
        return buildDetail(homework, course);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByTeacher(Long homeworkId) {
        Long teacherId = requireUserId();
        EduHomework homework = homeworkMapper.selectById(homeworkId);
        if (homework == null) {
            return;
        }
        requireTeacherCourse(homework.getCourseId(), teacherId);
        homeworkMapper.deleteById(homeworkId);
        homeworkProblemMapper.delete(new LambdaQueryWrapper<EduHomeworkProblem>()
                .eq(EduHomeworkProblem::getHomeworkId, homeworkId));
    }

    @Override
    public List<HomeworkItemVO> listMyHomework(Long courseId) {
        Long userId = requireUserId();
        Set<Long> accessibleCourseIds = queryAccessibleCourseIds(userId);
        if (accessibleCourseIds.isEmpty()) {
            return List.of();
        }
        if (courseId != null) {
            if (!accessibleCourseIds.contains(courseId)) {
                throw new IllegalArgumentException("无权限查看该课程作业");
            }
            accessibleCourseIds = Set.of(courseId);
        }

        List<EduCourse> courses = courseMapper.selectBatchIds(accessibleCourseIds);
        Map<Long, String> courseTitleMap = courses.stream()
                .collect(Collectors.toMap(EduCourse::getId, EduCourse::getTitle));

        List<EduHomework> homeworkList = homeworkMapper.selectList(new LambdaQueryWrapper<EduHomework>()
                .in(EduHomework::getCourseId, accessibleCourseIds)
                .orderByDesc(EduHomework::getId));
        return toHomeworkItems(homeworkList, courseTitleMap);
    }

    @Override
    public HomeworkDetailVO myHomeworkDetail(Long homeworkId) {
        Long userId = requireUserId();
        EduHomework homework = homeworkMapper.selectById(homeworkId);
        if (homework == null) {
            throw new IllegalArgumentException("作业不存在");
        }
        EduCourse course = courseMapper.selectById(homework.getCourseId());
        if (course == null) {
            throw new IllegalArgumentException("课程不存在");
        }
        boolean isTeacherOwner = userId.equals(course.getTeacherId());
        boolean isEnrolled = enrollMapper.selectCount(new LambdaQueryWrapper<EduCourseEnroll>()
                .eq(EduCourseEnroll::getUserId, userId)
                .eq(EduCourseEnroll::getCourseId, course.getId())
                .eq(EduCourseEnroll::getStatus, 1)) > 0;
        if (!isTeacherOwner && !isEnrolled) {
            throw new IllegalArgumentException("无权限查看该作业");
        }
        return buildDetail(homework, course);
    }

    private void saveHomeworkProblems(Long homeworkId, Long courseId, List<TeacherHomeworkProblemRequest> problems) {
        if (problems == null || problems.isEmpty()) {
            return;
        }
        Set<Long> ids = problems.stream()
                .map(TeacherHomeworkProblemRequest::getProblemId)
                .collect(Collectors.toSet());
        List<EduQuestion> questionList = questionMapper.selectBatchIds(ids);
        Map<Long, EduQuestion> questionMap = questionList.stream()
                .collect(Collectors.toMap(EduQuestion::getId, x -> x));
        for (TeacherHomeworkProblemRequest item : problems) {
            EduQuestion q = questionMap.get(item.getProblemId());
            if (q == null) {
                throw new IllegalArgumentException("题目不存在: " + item.getProblemId());
            }
            if (!courseId.equals(q.getCourseId())) {
                throw new IllegalArgumentException("题目不属于当前课程: " + item.getProblemId());
            }
            EduHomeworkProblem row = new EduHomeworkProblem();
            row.setHomeworkId(homeworkId);
            row.setProblemId(item.getProblemId());
            row.setScore(item.getScore());
            homeworkProblemMapper.insert(row);
        }
    }

    private HomeworkDetailVO buildDetail(EduHomework homework, EduCourse course) {
        List<EduHomeworkProblem> rows = homeworkProblemMapper.selectList(new LambdaQueryWrapper<EduHomeworkProblem>()
                .eq(EduHomeworkProblem::getHomeworkId, homework.getId())
                .orderByAsc(EduHomeworkProblem::getId));
        List<Long> problemIds = rows.stream()
                .map(EduHomeworkProblem::getProblemId)
                .collect(Collectors.toList());

        Map<Long, EduQuestion> questionMap = new HashMap<>();
        if (!problemIds.isEmpty()) {
            questionMap = questionMapper.selectBatchIds(problemIds).stream()
                    .collect(Collectors.toMap(EduQuestion::getId, x -> x));
        }

        List<HomeworkProblemVO> problemVOs = new ArrayList<>();
        int totalScore = 0;
        for (EduHomeworkProblem row : rows) {
            HomeworkProblemVO vo = new HomeworkProblemVO();
            vo.setProblemId(row.getProblemId());
            EduQuestion question = questionMap.get(row.getProblemId());
            vo.setTitle(question == null ? "题目已删除" : question.getTitle());
            vo.setDifficulty(question == null ? null : question.getDifficulty());
            vo.setScore(row.getScore() == null ? 0 : row.getScore());
            totalScore += vo.getScore() == null ? 0 : vo.getScore();
            problemVOs.add(vo);
        }

        HomeworkDetailVO detail = new HomeworkDetailVO();
        detail.setId(homework.getId());
        detail.setCourseId(homework.getCourseId());
        detail.setCourseTitle(course.getTitle());
        detail.setTitle(homework.getTitle());
        detail.setDeadline(homework.getDeadline());
        detail.setQuestionCount(problemVOs.size());
        detail.setTotalScore(totalScore);
        detail.setCreatedAt(homework.getCreatedAt());
        detail.setProblems(problemVOs);
        return detail;
    }

    private List<HomeworkItemVO> toHomeworkItems(List<EduHomework> homeworkList, Map<Long, String> courseTitleMap) {
        if (homeworkList.isEmpty()) {
            return List.of();
        }
        List<Long> ids = homeworkList.stream().map(EduHomework::getId).collect(Collectors.toList());
        List<EduHomeworkProblem> rows = homeworkProblemMapper.selectList(new LambdaQueryWrapper<EduHomeworkProblem>()
                .in(EduHomeworkProblem::getHomeworkId, ids));

        Map<Long, Integer> questionCountMap = new HashMap<>();
        Map<Long, Integer> totalScoreMap = new HashMap<>();
        for (EduHomeworkProblem row : rows) {
            Long homeworkId = row.getHomeworkId();
            questionCountMap.put(homeworkId, questionCountMap.getOrDefault(homeworkId, 0) + 1);
            totalScoreMap.put(homeworkId, totalScoreMap.getOrDefault(homeworkId, 0) + (row.getScore() == null ? 0 : row.getScore()));
        }

        List<HomeworkItemVO> list = new ArrayList<>();
        for (EduHomework hw : homeworkList) {
            HomeworkItemVO item = new HomeworkItemVO();
            item.setId(hw.getId());
            item.setCourseId(hw.getCourseId());
            item.setCourseTitle(courseTitleMap.getOrDefault(hw.getCourseId(), "-"));
            item.setTitle(hw.getTitle());
            item.setDeadline(hw.getDeadline());
            item.setQuestionCount(questionCountMap.getOrDefault(hw.getId(), 0));
            item.setTotalScore(totalScoreMap.getOrDefault(hw.getId(), 0));
            item.setCreatedAt(hw.getCreatedAt());
            list.add(item);
        }
        return list;
    }

    private Set<Long> queryAccessibleCourseIds(Long userId) {
        Set<Long> courseIds = new HashSet<>();
        List<EduCourseEnroll> enrolls = enrollMapper.selectList(new LambdaQueryWrapper<EduCourseEnroll>()
                .eq(EduCourseEnroll::getUserId, userId)
                .eq(EduCourseEnroll::getStatus, 1));
        for (EduCourseEnroll enroll : enrolls) {
            courseIds.add(enroll.getCourseId());
        }
        List<EduCourse> teacherCourses = courseMapper.selectList(new LambdaQueryWrapper<EduCourse>()
                .eq(EduCourse::getTeacherId, userId));
        for (EduCourse course : teacherCourses) {
            courseIds.add(course.getId());
        }
        return courseIds;
    }
}

