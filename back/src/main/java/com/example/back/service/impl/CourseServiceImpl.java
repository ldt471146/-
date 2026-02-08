package com.example.back.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.back.entity.EduChapter;
import com.example.back.entity.EduCourse;
import com.example.back.entity.EduCourseEnroll;
import com.example.back.entity.EduLesson;
import com.example.back.mapper.EduChapterMapper;
import com.example.back.mapper.EduCourseMapper;
import com.example.back.mapper.EduCourseEnrollMapper;
import com.example.back.mapper.EduLearnRecordMapper;
import com.example.back.mapper.EduLessonMapper;
import com.example.back.service.CourseService;
import com.example.back.vo.ChapterVO;
import com.example.back.vo.CourseDetailVO;
import com.example.back.vo.CourseVO;
import com.example.back.vo.LessonVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 课程服务实现
 */
@Service
public class CourseServiceImpl implements CourseService {

    private final EduCourseMapper courseMapper;
    private final EduChapterMapper chapterMapper;
    private final EduLessonMapper lessonMapper;
    private final EduCourseEnrollMapper enrollMapper;
    private final EduLearnRecordMapper learnRecordMapper;
    private final com.example.back.mapper.SysUserMapper userMapper;

    public CourseServiceImpl(EduCourseMapper courseMapper,
                             EduChapterMapper chapterMapper,
                             EduLessonMapper lessonMapper,
                             EduCourseEnrollMapper enrollMapper,
                             EduLearnRecordMapper learnRecordMapper,
                             com.example.back.mapper.SysUserMapper userMapper) {
        this.courseMapper = courseMapper;
        this.chapterMapper = chapterMapper;
        this.lessonMapper = lessonMapper;
        this.enrollMapper = enrollMapper;
        this.learnRecordMapper = learnRecordMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<CourseVO> listCourses() {
        List<EduCourse> courses = courseMapper.selectList(new LambdaQueryWrapper<EduCourse>()
                .eq(EduCourse::getStatus, 1)
                .orderByDesc(EduCourse::getId));
        return buildCourseVOs(courses);
    }

    @Override
    public List<CourseVO> listMyCourses(Long userId) {
        List<CourseVO> courses = courseMapper.selectMyCourses(userId);
        if (courses == null || courses.isEmpty()) {
            return courses;
        }
        List<Long> courseIds = courses.stream().map(CourseVO::getId).collect(Collectors.toList());
        List<EduCourse> courseEntities = courseMapper.selectBatchIds(courseIds);
        Map<Long, EduCourse> courseMap = courseEntities.stream()
                .collect(Collectors.toMap(EduCourse::getId, c -> c));
        java.util.Set<Long> teacherIds = courseEntities.stream()
                .map(EduCourse::getTeacherId)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
        java.util.Map<Long, String> teacherMap = userMapper.selectBatchIds(teacherIds)
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        com.example.back.entity.SysUser::getId,
                        com.example.back.entity.SysUser::getUsername
                ));
        Map<Long, com.example.back.vo.CourseLastLearnVO> lastLearnMap = learnRecordMapper.listLastLearnByUser(userId)
                .stream()
                .collect(Collectors.toMap(
                        com.example.back.vo.CourseLastLearnVO::getCourseId,
                        v -> v,
                        (a, b) -> a
                ));
        for (CourseVO vo : courses) {
            Integer total = lessonMapper.countByCourse(vo.getId());
            Integer finished = learnRecordMapper.countFinishedByCourse(userId, vo.getId());
            int totalVal = total == null ? 0 : total;
            int finishedVal = finished == null ? 0 : finished;
            vo.setTotalLessons(totalVal);
            vo.setFinishedLessons(finishedVal);
            vo.setProgress(totalVal == 0 ? 0 : (int) Math.round(finishedVal * 100.0 / totalVal));
            EduCourse entity = courseMap.get(vo.getId());
            if (entity != null) {
                vo.setFinishStatus(entity.getFinishStatus());
                vo.setCreatedAt(entity.getCreatedAt());
                vo.setUpdatedAt(entity.getUpdatedAt());
                vo.setTeacherName(teacherMap.getOrDefault(entity.getTeacherId(), "-"));
            }
            com.example.back.vo.CourseLastLearnVO last = lastLearnMap.get(vo.getId());
            if (last != null) {
                vo.setLastLessonId(last.getLessonId());
                vo.setLastLessonTitle(last.getLessonTitle());
                vo.setLastLearnAt(last.getLearnedAt());
            }
        }
        return courses;
    }

    @Override
    public CourseDetailVO getCourseDetail(Long courseId) {
        EduCourse course = courseMapper.selectById(courseId);
        if (course == null || course.getStatus() == null || course.getStatus() != 1) {
            return null;
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
        detail.setTeacherName(resolveTeacherName(course.getTeacherId()));
        detail.setChapters(chapterVOs);
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enrollCourse(Long userId, Long courseId) {
        EduCourse course = courseMapper.selectById(courseId);
        if (course == null || course.getStatus() == null || course.getStatus() != 1) {
            throw new IllegalArgumentException("课程不存在或已下架");
        }

        EduCourseEnroll existing = enrollMapper.selectOne(new LambdaQueryWrapper<EduCourseEnroll>()
                .eq(EduCourseEnroll::getUserId, userId)
                .eq(EduCourseEnroll::getCourseId, courseId));
        if (existing != null) {
            if (existing.getStatus() != null && existing.getStatus() == 1) {
                return;
            }
            existing.setStatus(1);
            enrollMapper.updateById(existing);
            return;
        }

        EduCourseEnroll enroll = new EduCourseEnroll();
        enroll.setUserId(userId);
        enroll.setCourseId(courseId);
        enroll.setStatus(1);
        enrollMapper.insert(enroll);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelEnroll(Long userId, Long courseId) {
        EduCourseEnroll existing = enrollMapper.selectOne(new LambdaQueryWrapper<EduCourseEnroll>()
                .eq(EduCourseEnroll::getUserId, userId)
                .eq(EduCourseEnroll::getCourseId, courseId));
        if (existing == null || existing.getStatus() == null || existing.getStatus() == 0) {
            return;
        }
        existing.setStatus(0);
        enrollMapper.updateById(existing);
    }

    private CourseVO toCourseVO(EduCourse course) {
        CourseVO vo = new CourseVO();
        vo.setId(course.getId());
        vo.setTitle(course.getTitle());
        vo.setCover(course.getCover());
        vo.setIntro(course.getIntro());
        vo.setFinishStatus(course.getFinishStatus());
        vo.setCreatedAt(course.getCreatedAt());
        vo.setUpdatedAt(course.getUpdatedAt());
        vo.setTeacherName(resolveTeacherName(course.getTeacherId()));
        vo.setTotalLessons(0);
        vo.setFinishedLessons(0);
        vo.setProgress(0);
        vo.setLastLessonId(null);
        vo.setLastLessonTitle(null);
        vo.setLastLearnAt(null);
        return vo;
    }

    private String resolveTeacherName(Long teacherId) {
        if (teacherId == null) {
            return "-";
        }
        com.example.back.entity.SysUser user = userMapper.selectById(teacherId);
        return user == null ? "-" : user.getUsername();
    }

    private List<CourseVO> buildCourseVOs(List<EduCourse> courses) {
        if (courses == null || courses.isEmpty()) {
            return List.of();
        }
        java.util.Set<Long> teacherIds = courses.stream()
                .map(EduCourse::getTeacherId)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
        java.util.Map<Long, String> teacherMap = userMapper.selectBatchIds(teacherIds)
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        com.example.back.entity.SysUser::getId,
                        com.example.back.entity.SysUser::getUsername
                ));
        return courses.stream().map(c -> {
            CourseVO vo = new CourseVO();
            vo.setId(c.getId());
            vo.setTitle(c.getTitle());
            vo.setCover(c.getCover());
            vo.setIntro(c.getIntro());
            vo.setFinishStatus(c.getFinishStatus());
            vo.setCreatedAt(c.getCreatedAt());
            vo.setUpdatedAt(c.getUpdatedAt());
            vo.setTeacherName(teacherMap.getOrDefault(c.getTeacherId(), "-"));
            vo.setTotalLessons(0);
            vo.setFinishedLessons(0);
            vo.setProgress(0);
            vo.setLastLessonId(null);
            vo.setLastLessonTitle(null);
            vo.setLastLearnAt(null);
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
}
