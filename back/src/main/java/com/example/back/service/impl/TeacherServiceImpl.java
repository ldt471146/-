package com.example.back.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.back.dto.TeacherChapterRequest;
import com.example.back.dto.TeacherCourseRequest;
import com.example.back.dto.TeacherQuestionImportItemRequest;
import com.example.back.dto.TeacherQuestionImportRequest;
import com.example.back.dto.TeacherLessonRequest;
import com.example.back.dto.TeacherQuestionOptionRequest;
import com.example.back.dto.TeacherQuestionRequest;
import com.example.back.dto.TeacherCodeProblemRequest;
import com.example.back.dto.TeacherCodeTestcaseRequest;
import com.example.back.entity.EduChapter;
import com.example.back.entity.EduCodeProblem;
import com.example.back.entity.EduCodeTestcase;
import com.example.back.entity.EduCourse;
import com.example.back.entity.EduLesson;
import com.example.back.entity.EduQuestion;
import com.example.back.entity.EduQuestionOption;
import com.example.back.mapper.EduChapterMapper;
import com.example.back.mapper.EduCodeProblemMapper;
import com.example.back.mapper.EduCodeTestcaseMapper;
import com.example.back.mapper.EduCourseMapper;
import com.example.back.mapper.EduLessonMapper;
import com.example.back.mapper.EduQuestionMapper;
import com.example.back.mapper.EduQuestionOptionMapper;
import com.example.back.mapper.TeacherStatsMapper;
import com.example.back.service.TeacherService;
import com.example.back.util.SecurityUtil;
import com.example.back.vo.ChapterVO;
import com.example.back.vo.CourseDetailVO;
import com.example.back.vo.LessonVO;
import com.example.back.vo.PageResultVO;
import com.example.back.vo.TeacherCourseVO;
import com.example.back.vo.TeacherCourseStatVO;
import com.example.back.vo.TeacherExamStatVO;
import com.example.back.vo.TeacherStatsOverviewVO;
import com.example.back.vo.TeacherStudentRankVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 教师端服务实现
 */
@Service
public class TeacherServiceImpl implements TeacherService {

    private final EduCourseMapper courseMapper;
    private final EduChapterMapper chapterMapper;
    private final EduLessonMapper lessonMapper;
    private final EduQuestionMapper questionMapper;
    private final EduQuestionOptionMapper optionMapper;
    private final EduCodeProblemMapper codeProblemMapper;
    private final EduCodeTestcaseMapper codeTestcaseMapper;
    private final TeacherStatsMapper teacherStatsMapper;

    public TeacherServiceImpl(EduCourseMapper courseMapper,
                              EduChapterMapper chapterMapper,
                              EduLessonMapper lessonMapper,
                              EduQuestionMapper questionMapper,
                              EduQuestionOptionMapper optionMapper,
                              EduCodeProblemMapper codeProblemMapper,
                              EduCodeTestcaseMapper codeTestcaseMapper,
                              TeacherStatsMapper teacherStatsMapper) {
        this.courseMapper = courseMapper;
        this.chapterMapper = chapterMapper;
        this.lessonMapper = lessonMapper;
        this.questionMapper = questionMapper;
        this.optionMapper = optionMapper;
        this.codeProblemMapper = codeProblemMapper;
        this.codeTestcaseMapper = codeTestcaseMapper;
        this.teacherStatsMapper = teacherStatsMapper;
    }

    private Long requireTeacherId() {
        Long userId = SecurityUtil.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        return userId;
    }

    private String normalizeUrl(String val) {
        if (val == null) {
            return null;
        }
        String raw = val.trim();
        if (raw.isEmpty()) {
            return "";
        }
        if (raw.startsWith("http://") || raw.startsWith("https://")) {
            return raw;
        }
        if (raw.startsWith("//")) {
            return "https:" + raw;
        }
        return "https://" + raw;
    }

    @Override
    public List<TeacherCourseVO> listMyCourses() {
        Long teacherId = requireTeacherId();
        List<EduCourse> courses = courseMapper.selectList(new LambdaQueryWrapper<EduCourse>()
                .eq(EduCourse::getTeacherId, teacherId)
                .orderByDesc(EduCourse::getId));
        return courses.stream().map(c -> {
            TeacherCourseVO vo = new TeacherCourseVO();
            vo.setId(c.getId());
            vo.setTitle(c.getTitle());
            vo.setCover(c.getCover());
            vo.setIntro(c.getIntro());
            vo.setStatus(c.getStatus());
            vo.setFinishStatus(c.getFinishStatus());
            vo.setCreatedAt(c.getCreatedAt());
            vo.setUpdatedAt(c.getUpdatedAt());
            Long chapterCount = chapterMapper.selectCount(new LambdaQueryWrapper<EduChapter>()
                    .eq(EduChapter::getCourseId, c.getId()));
            Integer lessonCount = lessonMapper.countByCourse(c.getId());
            vo.setChapterCount(chapterCount == null ? 0 : chapterCount.intValue());
            vo.setLessonCount(lessonCount == null ? 0 : lessonCount);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCourse(TeacherCourseRequest request) {
        Long teacherId = requireTeacherId();
        EduCourse course = new EduCourse();
        course.setTitle(request.getTitle());
        course.setCover(normalizeUrl(request.getCover()));
        course.setIntro(request.getIntro());
        course.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        course.setFinishStatus(request.getFinishStatus() == null ? 0 : request.getFinishStatus());
        course.setTeacherId(teacherId);
        courseMapper.insert(course);
        return course.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCourse(Long courseId, TeacherCourseRequest request) {
        Long teacherId = requireTeacherId();
        EduCourse course = courseMapper.selectById(courseId);
        if (course == null || !teacherId.equals(course.getTeacherId())) {
            throw new IllegalArgumentException("课程不存在或无权限");
        }
        course.setTitle(request.getTitle());
        course.setCover(normalizeUrl(request.getCover()));
        course.setIntro(request.getIntro());
        if (request.getStatus() != null) {
            course.setStatus(request.getStatus());
        }
        if (request.getFinishStatus() != null) {
            course.setFinishStatus(request.getFinishStatus());
        }
        courseMapper.updateById(course);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCourse(Long courseId) {
        Long teacherId = requireTeacherId();
        EduCourse course = courseMapper.selectById(courseId);
        if (course == null || !teacherId.equals(course.getTeacherId())) {
            throw new IllegalArgumentException("课程不存在或无权限");
        }
        courseMapper.deleteById(courseId);
    }

    @Override
    public CourseDetailVO courseDetail(Long courseId) {
        Long teacherId = requireTeacherId();
        EduCourse course = courseMapper.selectById(courseId);
        if (course == null || !teacherId.equals(course.getTeacherId())) {
            throw new IllegalArgumentException("课程不存在或无权限");
        }
        List<EduChapter> chapters = chapterMapper.selectList(new LambdaQueryWrapper<EduChapter>()
                .eq(EduChapter::getCourseId, courseId)
                .orderByAsc(EduChapter::getSortNo));
        List<EduLesson> lessons = lessonMapper.selectList(new LambdaQueryWrapper<EduLesson>()
                .in(!chapters.isEmpty(), EduLesson::getChapterId,
                        chapters.stream().map(EduChapter::getId).collect(Collectors.toList()))
                .orderByAsc(EduLesson::getSortNo));

        Map<Long, List<EduLesson>> lessonMap = lessons.stream()
                .collect(Collectors.groupingBy(EduLesson::getChapterId));
        List<ChapterVO> chapterVOs = chapters.stream()
                .sorted(Comparator.comparing(EduChapter::getSortNo))
                .map(ch -> {
                    ChapterVO vo = new ChapterVO();
                    vo.setId(ch.getId());
                    vo.setTitle(ch.getTitle());
                    vo.setSortNo(ch.getSortNo());
                    List<LessonVO> lessonVOs = lessonMap.getOrDefault(ch.getId(), List.of())
                            .stream()
                            .sorted(Comparator.comparing(EduLesson::getSortNo))
                            .map(this::toLessonVO)
                            .collect(Collectors.toList());
                    vo.setLessons(lessonVOs);
                    return vo;
                })
                .collect(Collectors.toList());

        CourseDetailVO detail = new CourseDetailVO();
        detail.setId(course.getId());
        detail.setTitle(course.getTitle());
        detail.setCover(course.getCover());
        detail.setIntro(course.getIntro());
        detail.setFinishStatus(course.getFinishStatus());
        detail.setCreatedAt(course.getCreatedAt());
        detail.setUpdatedAt(course.getUpdatedAt());
        detail.setTeacherName("-");
        detail.setChapters(chapterVOs);
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addChapter(Long courseId, TeacherChapterRequest request) {
        Long teacherId = requireTeacherId();
        EduCourse course = courseMapper.selectById(courseId);
        if (course == null || !teacherId.equals(course.getTeacherId())) {
            throw new IllegalArgumentException("课程不存在或无权限");
        }
        EduChapter chapter = new EduChapter();
        chapter.setCourseId(courseId);
        chapter.setTitle(request.getTitle());
        chapter.setSortNo(request.getSortNo());
        chapterMapper.insert(chapter);
        return chapter.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateChapter(Long chapterId, TeacherChapterRequest request) {
        Long teacherId = requireTeacherId();
        EduChapter chapter = chapterMapper.selectById(chapterId);
        if (chapter == null) {
            throw new IllegalArgumentException("章节不存在");
        }
        EduCourse course = courseMapper.selectById(chapter.getCourseId());
        if (course == null || !teacherId.equals(course.getTeacherId())) {
            throw new IllegalArgumentException("无权限");
        }
        chapter.setTitle(request.getTitle());
        chapter.setSortNo(request.getSortNo());
        chapterMapper.updateById(chapter);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteChapter(Long chapterId) {
        Long teacherId = requireTeacherId();
        EduChapter chapter = chapterMapper.selectById(chapterId);
        if (chapter == null) {
            return;
        }
        EduCourse course = courseMapper.selectById(chapter.getCourseId());
        if (course == null || !teacherId.equals(course.getTeacherId())) {
            throw new IllegalArgumentException("无权限");
        }
        chapterMapper.deleteById(chapterId);
        lessonMapper.delete(new LambdaQueryWrapper<EduLesson>()
                .eq(EduLesson::getChapterId, chapterId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addLesson(Long chapterId, TeacherLessonRequest request) {
        Long teacherId = requireTeacherId();
        EduChapter chapter = chapterMapper.selectById(chapterId);
        if (chapter == null) {
            throw new IllegalArgumentException("章节不存在");
        }
        EduCourse course = courseMapper.selectById(chapter.getCourseId());
        if (course == null || !teacherId.equals(course.getTeacherId())) {
            throw new IllegalArgumentException("无权限");
        }
        EduLesson lesson = new EduLesson();
        lesson.setChapterId(chapterId);
        lesson.setTitle(request.getTitle());
        lesson.setContentType(request.getContentType());
        lesson.setContentUrl(request.getContentUrl());
        lesson.setContentText(request.getContentText());
        lesson.setSortNo(request.getSortNo());
        lessonMapper.insert(lesson);
        return lesson.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLesson(Long lessonId, TeacherLessonRequest request) {
        Long teacherId = requireTeacherId();
        EduLesson lesson = lessonMapper.selectById(lessonId);
        if (lesson == null) {
            throw new IllegalArgumentException("课时不存在");
        }
        EduChapter chapter = chapterMapper.selectById(lesson.getChapterId());
        EduCourse course = chapter == null ? null : courseMapper.selectById(chapter.getCourseId());
        if (course == null || !teacherId.equals(course.getTeacherId())) {
            throw new IllegalArgumentException("无权限");
        }
        lesson.setTitle(request.getTitle());
        lesson.setContentType(request.getContentType());
        lesson.setContentUrl(request.getContentUrl());
        lesson.setContentText(request.getContentText());
        lesson.setSortNo(request.getSortNo());
        lessonMapper.updateById(lesson);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLesson(Long lessonId) {
        Long teacherId = requireTeacherId();
        EduLesson lesson = lessonMapper.selectById(lessonId);
        if (lesson == null) {
            return;
        }
        EduChapter chapter = chapterMapper.selectById(lesson.getChapterId());
        EduCourse course = chapter == null ? null : courseMapper.selectById(chapter.getCourseId());
        if (course == null || !teacherId.equals(course.getTeacherId())) {
            throw new IllegalArgumentException("无权限");
        }
        lessonMapper.deleteById(lessonId);
    }

    @Override
    public PageResultVO<com.example.back.vo.TeacherQuestionVO> listQuestions(Long courseId, Long chapterId, long page, long size) {
        Long teacherId = requireTeacherId();
        if (courseId == null) {
            throw new IllegalArgumentException("请选择课程");
        }
        EduCourse course = courseMapper.selectById(courseId);
        if (course == null || !teacherId.equals(course.getTeacherId())) {
            throw new IllegalArgumentException("无权限");
        }
        Page<EduQuestion> mpPage = new Page<>(page, size);
        Page<EduQuestion> result = questionMapper.selectPage(mpPage, new LambdaQueryWrapper<EduQuestion>()
                .eq(EduQuestion::getCourseId, courseId)
                .eq(chapterId != null, EduQuestion::getChapterId, chapterId)
                .orderByDesc(EduQuestion::getId));
        List<com.example.back.vo.TeacherQuestionVO> records = buildTeacherQuestionVOs(result.getRecords());
        PageResultVO<com.example.back.vo.TeacherQuestionVO> pageResult = new PageResultVO<>();
        pageResult.setPage(page);
        pageResult.setSize(size);
        pageResult.setTotal(result.getTotal());
        pageResult.setRecords(records);
        return pageResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createQuestion(TeacherQuestionRequest request) {
        Long teacherId = requireTeacherId();
        if (request.getCourseId() == null) {
            throw new IllegalArgumentException("请选择课程");
        }
        EduCourse course = courseMapper.selectById(request.getCourseId());
        if (course == null || !teacherId.equals(course.getTeacherId())) {
            throw new IllegalArgumentException("无权限");
        }
        if (request.getChapterId() != null) {
            EduChapter chapter = chapterMapper.selectById(request.getChapterId());
            if (chapter == null || !request.getCourseId().equals(chapter.getCourseId())) {
                throw new IllegalArgumentException("章节不属于该课程");
            }
        }
        EduQuestion question = new EduQuestion();
        question.setTitle(request.getTitle());
        question.setType(request.getType());
        question.setAnalysis(request.getAnalysis());
        question.setDifficulty(request.getDifficulty());
        question.setCourseId(request.getCourseId());
        question.setChapterId(request.getChapterId());
        questionMapper.insert(question);
        insertOptions(question.getId(), request.getOptions());
        return question.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQuestion(Long id, TeacherQuestionRequest request) {
        Long teacherId = requireTeacherId();
        EduQuestion question = questionMapper.selectById(id);
        if (question == null) {
            throw new IllegalArgumentException("题目不存在");
        }
        EduCourse course = courseMapper.selectById(question.getCourseId());
        if (course == null || !teacherId.equals(course.getTeacherId())) {
            throw new IllegalArgumentException("无权限");
        }
        if (request.getChapterId() != null) {
            EduChapter chapter = chapterMapper.selectById(request.getChapterId());
            if (chapter == null || !question.getCourseId().equals(chapter.getCourseId())) {
                throw new IllegalArgumentException("章节不属于该课程");
            }
        }
        question.setTitle(request.getTitle());
        question.setType(request.getType());
        question.setAnalysis(request.getAnalysis());
        question.setDifficulty(request.getDifficulty());
        question.setChapterId(request.getChapterId());
        questionMapper.updateById(question);
        optionMapper.delete(new LambdaQueryWrapper<EduQuestionOption>()
                .eq(EduQuestionOption::getQuestionId, id));
        insertOptions(id, request.getOptions());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteQuestion(Long id) {
        Long teacherId = requireTeacherId();
        EduQuestion question = questionMapper.selectById(id);
        if (question == null) {
            return;
        }
        EduCourse course = courseMapper.selectById(question.getCourseId());
        if (course == null || !teacherId.equals(course.getTeacherId())) {
            throw new IllegalArgumentException("无权限");
        }
        questionMapper.deleteById(id);
        optionMapper.delete(new LambdaQueryWrapper<EduQuestionOption>()
                .eq(EduQuestionOption::getQuestionId, id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int importQuestions(TeacherQuestionImportRequest request) {
        Long teacherId = requireTeacherId();
        EduCourse course = courseMapper.selectById(request.getCourseId());
        if (course == null || !teacherId.equals(course.getTeacherId())) {
            throw new IllegalArgumentException("课程不存在或无权限");
        }
        if (request.getChapterId() != null) {
            EduChapter chapter = chapterMapper.selectById(request.getChapterId());
            if (chapter == null || !request.getCourseId().equals(chapter.getCourseId())) {
                throw new IllegalArgumentException("章节不属于该课程");
            }
        }
        List<TeacherQuestionImportItemRequest> items = request.getItems();
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("导入内容为空");
        }
        int count = 0;
        for (int i = 0; i < items.size(); i++) {
            TeacherQuestionImportItemRequest item = items.get(i);
            if (item == null || item.getTitle() == null || item.getTitle().isBlank()) {
                throw new IllegalArgumentException("第 " + (i + 1) + " 题标题不能为空");
            }
            if (item.getOptions() == null || item.getOptions().isEmpty()) {
                throw new IllegalArgumentException("第 " + (i + 1) + " 题选项不能为空");
            }
            String type = (item.getType() == null || item.getType().isBlank()) ? "single" : item.getType();
            if (!"single".equals(type) && !"multi".equals(type)) {
                throw new IllegalArgumentException("第 " + (i + 1) + " 题题型不合法，仅支持 single/multi");
            }
            long correctCount = item.getOptions().stream()
                    .filter(o -> o != null && o.getIsCorrect() != null && o.getIsCorrect() == 1)
                    .count();
            if ("single".equals(type) && correctCount != 1) {
                throw new IllegalArgumentException("第 " + (i + 1) + " 题为单选题，必须且仅有 1 个正确选项");
            }
            if ("multi".equals(type) && correctCount < 1) {
                throw new IllegalArgumentException("第 " + (i + 1) + " 题为多选题，至少 1 个正确选项");
            }

            EduQuestion question = new EduQuestion();
            question.setTitle(item.getTitle().trim());
            question.setType(type);
            question.setAnalysis(item.getAnalysis());
            question.setDifficulty(item.getDifficulty() == null ? 1 : item.getDifficulty());
            question.setCourseId(request.getCourseId());
            question.setChapterId(request.getChapterId());
            questionMapper.insert(question);

            List<TeacherQuestionOptionRequest> normalizedOptions = normalizeOptions(item.getOptions());
            insertOptions(question.getId(), normalizedOptions);
            count++;
        }
        return count;
    }

    private List<TeacherQuestionOptionRequest> normalizeOptions(List<TeacherQuestionOptionRequest> options) {
        List<String> labels = List.of("A", "B", "C", "D", "E", "F");
        for (int i = 0; i < options.size(); i++) {
            TeacherQuestionOptionRequest opt = options.get(i);
            if (opt == null) {
                throw new IllegalArgumentException("选项内容不能为空");
            }
            if (opt.getContent() == null || opt.getContent().isBlank()) {
                throw new IllegalArgumentException("选项内容不能为空");
            }
            if (opt.getLabel() == null || opt.getLabel().isBlank()) {
                opt.setLabel(i < labels.size() ? labels.get(i) : "X" + (i + 1));
            }
            if (opt.getIsCorrect() == null) {
                opt.setIsCorrect(0);
            }
        }
        return options;
    }

    private void insertOptions(Long questionId, List<TeacherQuestionOptionRequest> options) {
        if (options == null || options.isEmpty()) {
            return;
        }
        for (TeacherQuestionOptionRequest opt : options) {
            EduQuestionOption option = new EduQuestionOption();
            option.setQuestionId(questionId);
            option.setLabel(opt.getLabel());
            option.setContent(opt.getContent());
            option.setIsCorrect(opt.getIsCorrect());
            optionMapper.insert(option);
        }
    }

    private List<com.example.back.vo.TeacherQuestionVO> buildTeacherQuestionVOs(List<EduQuestion> questions) {
        if (questions == null || questions.isEmpty()) {
            return List.of();
        }
        List<Long> ids = questions.stream().map(EduQuestion::getId).collect(Collectors.toList());
        List<EduQuestionOption> options = optionMapper.selectList(new LambdaQueryWrapper<EduQuestionOption>()
                .in(EduQuestionOption::getQuestionId, ids)
                .orderByAsc(EduQuestionOption::getLabel));
        Map<Long, List<EduQuestionOption>> optionMap = options.stream()
                .collect(Collectors.groupingBy(EduQuestionOption::getQuestionId));
        return questions.stream().map(q -> {
            com.example.back.vo.TeacherQuestionVO vo = new com.example.back.vo.TeacherQuestionVO();
            vo.setId(q.getId());
            vo.setTitle(q.getTitle());
            vo.setType(q.getType());
            vo.setDifficulty(q.getDifficulty());
            vo.setCourseId(q.getCourseId());
            vo.setChapterId(q.getChapterId());
            vo.setAnalysis(q.getAnalysis());
            vo.setOptions(optionMap.getOrDefault(q.getId(), List.of()).stream().map(opt -> {
                com.example.back.vo.TeacherQuestionOptionVO o = new com.example.back.vo.TeacherQuestionOptionVO();
                o.setLabel(opt.getLabel());
                o.setContent(opt.getContent());
                o.setIsCorrect(opt.getIsCorrect());
                return o;
            }).collect(Collectors.toList()));
            return vo;
        }).collect(Collectors.toList());
    }

    private LessonVO toLessonVO(EduLesson lesson) {
        LessonVO vo = new LessonVO();
        vo.setId(lesson.getId());
        vo.setTitle(lesson.getTitle());
        vo.setContentType(lesson.getContentType());
        vo.setContentUrl(lesson.getContentUrl());
        vo.setContentText(lesson.getContentText());
        vo.setSortNo(lesson.getSortNo());
        return vo;
    }

    @Override
    public PageResultVO<com.example.back.vo.TeacherCodeProblemVO> listCodeProblems(Long courseId, Long chapterId, long page, long size) {
        Long teacherId = requireTeacherId();
        if (courseId == null) {
            throw new IllegalArgumentException("请选择课程");
        }
        EduCourse course = courseMapper.selectById(courseId);
        if (course == null || !teacherId.equals(course.getTeacherId())) {
            throw new IllegalArgumentException("无权限");
        }
        Page<EduCodeProblem> mpPage = new Page<>(page, size);
        Page<EduCodeProblem> result = codeProblemMapper.selectPage(mpPage, new LambdaQueryWrapper<EduCodeProblem>()
                .eq(EduCodeProblem::getTeacherId, teacherId)
                .eq(EduCodeProblem::getCourseId, courseId)
                .eq(chapterId != null, EduCodeProblem::getChapterId, chapterId)
                .orderByDesc(EduCodeProblem::getId));
        List<com.example.back.vo.TeacherCodeProblemVO> records = buildTeacherCodeProblemVOs(result.getRecords());
        PageResultVO<com.example.back.vo.TeacherCodeProblemVO> pageResult = new PageResultVO<>();
        pageResult.setPage(page);
        pageResult.setSize(size);
        pageResult.setTotal(result.getTotal());
        pageResult.setRecords(records);
        return pageResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCodeProblem(TeacherCodeProblemRequest request) {
        Long teacherId = requireTeacherId();
        if (request.getCourseId() == null) {
            throw new IllegalArgumentException("请选择课程");
        }
        EduCourse course = courseMapper.selectById(request.getCourseId());
        if (course == null || !teacherId.equals(course.getTeacherId())) {
            throw new IllegalArgumentException("课程不存在或无权限");
        }
        if (request.getChapterId() != null) {
            EduChapter chapter = chapterMapper.selectById(request.getChapterId());
            if (chapter == null || !request.getCourseId().equals(chapter.getCourseId())) {
                throw new IllegalArgumentException("章节不属于该课程");
            }
        }
        EduCodeProblem problem = new EduCodeProblem();
        problem.setTeacherId(teacherId);
        problem.setCourseId(request.getCourseId());
        problem.setChapterId(request.getChapterId());
        problem.setTitle(request.getTitle());
        problem.setContent(request.getContent());
        problem.setDifficulty(request.getDifficulty() == null ? 1 : request.getDifficulty());
        problem.setTimeLimit(request.getTimeLimit() == null ? 1000 : request.getTimeLimit());
        problem.setMemoryLimit(request.getMemoryLimit() == null ? 256 : request.getMemoryLimit());
        problem.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        codeProblemMapper.insert(problem);
        saveCodeTestcases(problem.getId(), request.getTestcases());
        return problem.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCodeProblem(Long id, TeacherCodeProblemRequest request) {
        Long teacherId = requireTeacherId();
        EduCodeProblem problem = codeProblemMapper.selectById(id);
        if (problem == null) {
            throw new IllegalArgumentException("编程题不存在");
        }
        if (!teacherId.equals(problem.getTeacherId())) {
            throw new IllegalArgumentException("无权限");
        }
        Long finalCourseId = request.getCourseId() == null ? problem.getCourseId() : request.getCourseId();
        EduCourse course = courseMapper.selectById(finalCourseId);
        if (course == null || !teacherId.equals(course.getTeacherId())) {
            throw new IllegalArgumentException("课程不存在或无权限");
        }
        if (request.getChapterId() != null) {
            EduChapter chapter = chapterMapper.selectById(request.getChapterId());
            if (chapter == null || !finalCourseId.equals(chapter.getCourseId())) {
                throw new IllegalArgumentException("章节不属于该课程");
            }
        }
        problem.setCourseId(finalCourseId);
        problem.setChapterId(request.getChapterId());
        problem.setTitle(request.getTitle());
        problem.setContent(request.getContent());
        problem.setDifficulty(request.getDifficulty() == null ? 1 : request.getDifficulty());
        problem.setTimeLimit(request.getTimeLimit() == null ? 1000 : request.getTimeLimit());
        problem.setMemoryLimit(request.getMemoryLimit() == null ? 256 : request.getMemoryLimit());
        if (request.getStatus() != null) {
            problem.setStatus(request.getStatus());
        }
        codeProblemMapper.updateById(problem);
        codeTestcaseMapper.delete(new LambdaQueryWrapper<EduCodeTestcase>()
                .eq(EduCodeTestcase::getProblemId, id));
        saveCodeTestcases(id, request.getTestcases());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCodeProblem(Long id) {
        Long teacherId = requireTeacherId();
        EduCodeProblem problem = codeProblemMapper.selectById(id);
        if (problem == null) {
            return;
        }
        if (!teacherId.equals(problem.getTeacherId())) {
            throw new IllegalArgumentException("无权限");
        }
        codeProblemMapper.deleteById(id);
        codeTestcaseMapper.delete(new LambdaQueryWrapper<EduCodeTestcase>()
                .eq(EduCodeTestcase::getProblemId, id));
    }

    private void saveCodeTestcases(Long problemId, List<TeacherCodeTestcaseRequest> testcases) {
        if (testcases == null || testcases.isEmpty()) {
            return;
        }
        for (TeacherCodeTestcaseRequest tc : testcases) {
            if (tc == null) {
                continue;
            }
            EduCodeTestcase row = new EduCodeTestcase();
            row.setProblemId(problemId);
            row.setInputData(tc.getInputData());
            row.setOutputData(tc.getOutputData());
            row.setIsSample(tc.getIsSample() == null ? 0 : tc.getIsSample());
            codeTestcaseMapper.insert(row);
        }
    }

    private List<com.example.back.vo.TeacherCodeProblemVO> buildTeacherCodeProblemVOs(List<EduCodeProblem> problems) {
        if (problems == null || problems.isEmpty()) {
            return List.of();
        }
        List<Long> ids = problems.stream().map(EduCodeProblem::getId).collect(Collectors.toList());
        List<EduCodeTestcase> testcases = codeTestcaseMapper.selectList(new LambdaQueryWrapper<EduCodeTestcase>()
                .in(EduCodeTestcase::getProblemId, ids)
                .orderByAsc(EduCodeTestcase::getId));
        Map<Long, List<EduCodeTestcase>> testcaseMap = testcases.stream()
                .collect(Collectors.groupingBy(EduCodeTestcase::getProblemId));
        return problems.stream().map(p -> {
            com.example.back.vo.TeacherCodeProblemVO vo = new com.example.back.vo.TeacherCodeProblemVO();
            vo.setId(p.getId());
            vo.setCourseId(p.getCourseId());
            vo.setChapterId(p.getChapterId());
            vo.setTitle(p.getTitle());
            vo.setContent(p.getContent());
            vo.setDifficulty(p.getDifficulty());
            vo.setTimeLimit(p.getTimeLimit());
            vo.setMemoryLimit(p.getMemoryLimit());
            vo.setStatus(p.getStatus());
            vo.setTestcases(testcaseMap.getOrDefault(p.getId(), List.of()).stream().map(tc -> {
                com.example.back.vo.TeacherCodeTestcaseVO x = new com.example.back.vo.TeacherCodeTestcaseVO();
                x.setId(tc.getId());
                x.setInputData(tc.getInputData());
                x.setOutputData(tc.getOutputData());
                x.setIsSample(tc.getIsSample());
                return x;
            }).collect(Collectors.toList()));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public TeacherStatsOverviewVO statsOverview() {
        Long teacherId = requireTeacherId();
        Integer totalCourses = teacherStatsMapper.countCourses(teacherId);
        Integer totalStudents = teacherStatsMapper.countStudents(teacherId);
        Integer totalSubmissions = teacherStatsMapper.countSubmissions(teacherId);
        Double avgScore = teacherStatsMapper.avgScore(teacherId);
        List<TeacherStudentRankVO> ranks = teacherStatsMapper.studentRanks(teacherId);
        List<TeacherCourseStatVO> courseStats = teacherStatsMapper.courseStats(teacherId);
        List<TeacherExamStatVO> examStats = teacherStatsMapper.examStats(teacherId);

        TeacherStatsOverviewVO vo = new TeacherStatsOverviewVO();
        vo.setTotalCourses(totalCourses == null ? 0 : totalCourses);
        vo.setTotalStudents(totalStudents == null ? 0 : totalStudents);
        vo.setTotalSubmissions(totalSubmissions == null ? 0 : totalSubmissions);
        vo.setAvgScore(avgScore == null ? 0 : avgScore);
        vo.setStudentRanks(ranks == null ? List.of() : ranks);
        vo.setCourseStats(courseStats == null ? List.of() : courseStats);
        vo.setExamStats(examStats == null ? List.of() : examStats);
        return vo;
    }

    @Override
    public byte[] exportStatsCsv(String type) {
        TeacherStatsOverviewVO overview = statsOverview();
        String mode = type == null ? "students" : type.trim().toLowerCase();
        StringBuilder sb = new StringBuilder();
        if ("courses".equals(mode)) {
            sb.append("courseId,courseTitle,studentCount,avgScore,totalLearnMinutes\n");
            for (TeacherCourseStatVO row : overview.getCourseStats()) {
                sb.append(csv(row.getCourseId()))
                        .append(',').append(csv(row.getCourseTitle()))
                        .append(',').append(csv(row.getStudentCount()))
                        .append(',').append(csv(row.getAvgScore()))
                        .append(',').append(csv(row.getTotalLearnMinutes()))
                        .append('\n');
            }
        } else if ("tasks".equals(mode)) {
            sb.append("taskId,taskTitle,attempts,passCount,passRate\n");
            for (TeacherExamStatVO row : overview.getExamStats()) {
                sb.append(csv(row.getTaskId()))
                        .append(',').append(csv(row.getTaskTitle()))
                        .append(',').append(csv(row.getAttempts()))
                        .append(',').append(csv(row.getPassCount()))
                        .append(',').append(csv(row.getPassRate()))
                        .append('\n');
            }
        } else {
            sb.append("userId,username,learnMinutes,avgScore,submissionCount\n");
            for (TeacherStudentRankVO row : overview.getStudentRanks()) {
                sb.append(csv(row.getUserId()))
                        .append(',').append(csv(row.getUsername()))
                        .append(',').append(csv(row.getLearnMinutes()))
                        .append(',').append(csv(row.getAvgScore()))
                        .append(',').append(csv(row.getSubmissionCount()))
                        .append('\n');
            }
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String csv(Object val) {
        if (val == null) {
            return "";
        }
        String raw = String.valueOf(val).replace("\"", "\"\"");
        return "\"" + raw + "\"";
    }
}
