package com.example.back.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.back.dto.LearningPathProgressRequest;
import com.example.back.dto.TeacherKnowledgeDependencyRequest;
import com.example.back.dto.TeacherKnowledgePointRequest;
import com.example.back.entity.EduChapter;
import com.example.back.entity.EduCourse;
import com.example.back.entity.EduKnowledgeDependency;
import com.example.back.entity.EduKnowledgePoint;
import com.example.back.entity.EduKnowledgeProgress;
import com.example.back.mapper.EduChapterMapper;
import com.example.back.mapper.EduCourseMapper;
import com.example.back.mapper.EduKnowledgeDependencyMapper;
import com.example.back.mapper.EduKnowledgePointMapper;
import com.example.back.mapper.EduKnowledgeProgressMapper;
import com.example.back.service.LearningPathService;
import com.example.back.util.SecurityUtil;
import com.example.back.vo.LearningPathOverviewVO;
import com.example.back.vo.LearningPathPointVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LearningPathServiceImpl implements LearningPathService {

    private final EduKnowledgePointMapper pointMapper;
    private final EduKnowledgeDependencyMapper dependencyMapper;
    private final EduKnowledgeProgressMapper progressMapper;
    private final EduCourseMapper courseMapper;
    private final EduChapterMapper chapterMapper;

    public LearningPathServiceImpl(EduKnowledgePointMapper pointMapper,
                                   EduKnowledgeDependencyMapper dependencyMapper,
                                   EduKnowledgeProgressMapper progressMapper,
                                   EduCourseMapper courseMapper,
                                   EduChapterMapper chapterMapper) {
        this.pointMapper = pointMapper;
        this.dependencyMapper = dependencyMapper;
        this.progressMapper = progressMapper;
        this.courseMapper = courseMapper;
        this.chapterMapper = chapterMapper;
    }

    private Long requireUserId() {
        Long userId = SecurityUtil.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        return userId;
    }

    private EduCourse requireOwnedCourse(Long courseId, Long teacherId) {
        EduCourse course = courseMapper.selectById(courseId);
        if (course == null || !teacherId.equals(course.getTeacherId())) {
            throw new IllegalArgumentException("课程不存在或无权限");
        }
        return course;
    }

    @Override
    public List<EduKnowledgePoint> listTeacherPoints(Long courseId) {
        Long teacherId = requireUserId();
        requireOwnedCourse(courseId, teacherId);
        return pointMapper.selectList(new LambdaQueryWrapper<EduKnowledgePoint>()
                .eq(EduKnowledgePoint::getCourseId, courseId)
                .orderByAsc(EduKnowledgePoint::getSortNo)
                .orderByAsc(EduKnowledgePoint::getId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTeacherPoint(Long courseId, TeacherKnowledgePointRequest request) {
        Long teacherId = requireUserId();
        requireOwnedCourse(courseId, teacherId);
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("知识点标题不能为空");
        }
        if (request.getChapterId() != null) {
            EduChapter chapter = chapterMapper.selectById(request.getChapterId());
            if (chapter == null || !courseId.equals(chapter.getCourseId())) {
                throw new IllegalArgumentException("章节不属于该课程");
            }
        }
        EduKnowledgePoint point = new EduKnowledgePoint();
        point.setCourseId(courseId);
        point.setChapterId(request.getChapterId());
        point.setTitle(request.getTitle().trim());
        point.setDescription(request.getDescription());
        point.setSortNo(request.getSortNo() == null ? 0 : request.getSortNo());
        point.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        pointMapper.insert(point);
        return point.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTeacherPoint(Long pointId, TeacherKnowledgePointRequest request) {
        Long teacherId = requireUserId();
        EduKnowledgePoint point = pointMapper.selectById(pointId);
        if (point == null) {
            throw new IllegalArgumentException("知识点不存在");
        }
        requireOwnedCourse(point.getCourseId(), teacherId);
        if (request.getChapterId() != null) {
            EduChapter chapter = chapterMapper.selectById(request.getChapterId());
            if (chapter == null || !point.getCourseId().equals(chapter.getCourseId())) {
                throw new IllegalArgumentException("章节不属于该课程");
            }
        }
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            point.setTitle(request.getTitle().trim());
        }
        point.setChapterId(request.getChapterId());
        point.setDescription(request.getDescription());
        if (request.getSortNo() != null) {
            point.setSortNo(request.getSortNo());
        }
        if (request.getStatus() != null) {
            point.setStatus(request.getStatus());
        }
        pointMapper.updateById(point);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTeacherPoint(Long pointId) {
        Long teacherId = requireUserId();
        EduKnowledgePoint point = pointMapper.selectById(pointId);
        if (point == null) {
            return;
        }
        requireOwnedCourse(point.getCourseId(), teacherId);
        dependencyMapper.delete(new LambdaQueryWrapper<EduKnowledgeDependency>()
                .eq(EduKnowledgeDependency::getFromPointId, pointId)
                .or()
                .eq(EduKnowledgeDependency::getToPointId, pointId));
        progressMapper.delete(new LambdaQueryWrapper<EduKnowledgeProgress>()
                .eq(EduKnowledgeProgress::getPointId, pointId));
        pointMapper.deleteById(pointId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDependency(TeacherKnowledgeDependencyRequest request) {
        Long teacherId = requireUserId();
        if (request.getFromPointId() == null || request.getToPointId() == null) {
            throw new IllegalArgumentException("依赖参数不能为空");
        }
        if (request.getFromPointId().equals(request.getToPointId())) {
            throw new IllegalArgumentException("依赖关系不能指向自己");
        }
        EduKnowledgePoint from = pointMapper.selectById(request.getFromPointId());
        EduKnowledgePoint to = pointMapper.selectById(request.getToPointId());
        if (from == null || to == null) {
            throw new IllegalArgumentException("知识点不存在");
        }
        if (!from.getCourseId().equals(to.getCourseId())) {
            throw new IllegalArgumentException("只能配置同课程知识点依赖");
        }
        requireOwnedCourse(from.getCourseId(), teacherId);
        Long exists = dependencyMapper.selectCount(new LambdaQueryWrapper<EduKnowledgeDependency>()
                .eq(EduKnowledgeDependency::getFromPointId, request.getFromPointId())
                .eq(EduKnowledgeDependency::getToPointId, request.getToPointId()));
        if (exists != null && exists > 0) {
            throw new IllegalArgumentException("依赖关系已存在");
        }
        EduKnowledgeDependency row = new EduKnowledgeDependency();
        row.setFromPointId(request.getFromPointId());
        row.setToPointId(request.getToPointId());
        dependencyMapper.insert(row);
        return row.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDependency(Long id) {
        EduKnowledgeDependency dep = dependencyMapper.selectById(id);
        if (dep == null) {
            return;
        }
        EduKnowledgePoint from = pointMapper.selectById(dep.getFromPointId());
        Long teacherId = requireUserId();
        if (from == null) {
            dependencyMapper.deleteById(id);
            return;
        }
        requireOwnedCourse(from.getCourseId(), teacherId);
        dependencyMapper.deleteById(id);
    }

    @Override
    public LearningPathOverviewVO path(Long courseId) {
        Long userId = requireUserId();
        EduCourse course = courseMapper.selectById(courseId);
        if (course == null || course.getStatus() == null || course.getStatus() != 1) {
            throw new IllegalArgumentException("课程不存在或未开放");
        }
        List<EduKnowledgePoint> points = pointMapper.selectList(new LambdaQueryWrapper<EduKnowledgePoint>()
                .eq(EduKnowledgePoint::getCourseId, courseId)
                .eq(EduKnowledgePoint::getStatus, 1)
                .orderByAsc(EduKnowledgePoint::getSortNo)
                .orderByAsc(EduKnowledgePoint::getId));
        if (points.isEmpty()) {
            LearningPathOverviewVO empty = new LearningPathOverviewVO();
            empty.setCourseId(courseId);
            empty.setTotalPoints(0);
            empty.setLearnedPoints(0);
            empty.setPoints(List.of());
            return empty;
        }

        List<Long> pointIds = points.stream().map(EduKnowledgePoint::getId).collect(Collectors.toList());
        List<EduKnowledgeDependency> deps = dependencyMapper.selectList(new LambdaQueryWrapper<EduKnowledgeDependency>()
                .in(EduKnowledgeDependency::getFromPointId, pointIds)
                .in(EduKnowledgeDependency::getToPointId, pointIds));
        Map<Long, List<Long>> preMap = new LinkedHashMap<>();
        for (EduKnowledgePoint p : points) {
            preMap.put(p.getId(), new ArrayList<>());
        }
        for (EduKnowledgeDependency dep : deps) {
            preMap.computeIfAbsent(dep.getToPointId(), k -> new ArrayList<>()).add(dep.getFromPointId());
        }

        Set<Long> learnedSet = progressMapper.selectList(new LambdaQueryWrapper<EduKnowledgeProgress>()
                        .eq(EduKnowledgeProgress::getUserId, userId)
                        .eq(EduKnowledgeProgress::getStatus, 1)
                        .in(EduKnowledgeProgress::getPointId, pointIds))
                .stream()
                .map(EduKnowledgeProgress::getPointId)
                .collect(Collectors.toSet());

        Map<Long, String> chapterMap = chapterMapper.selectBatchIds(
                        points.stream()
                                .map(EduKnowledgePoint::getChapterId)
                                .filter(v -> v != null)
                                .collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(EduChapter::getId, EduChapter::getTitle));

        List<LearningPathPointVO> pointVOs = new ArrayList<>();
        Long nextPointId = null;
        String nextPointTitle = null;
        for (EduKnowledgePoint point : points) {
            LearningPathPointVO vo = new LearningPathPointVO();
            vo.setPointId(point.getId());
            vo.setChapterId(point.getChapterId());
            vo.setChapterTitle(point.getChapterId() == null ? "-" : chapterMap.getOrDefault(point.getChapterId(), "-"));
            vo.setTitle(point.getTitle());
            vo.setDescription(point.getDescription());
            vo.setSortNo(point.getSortNo());
            List<Long> pres = preMap.getOrDefault(point.getId(), List.of());
            vo.setPrerequisitePointIds(pres);

            String status;
            if (learnedSet.contains(point.getId())) {
                status = "LEARNED";
            } else {
                boolean unlocked = pres.isEmpty() || learnedSet.containsAll(pres);
                status = unlocked ? "UNLOCKED" : "LOCKED";
                if (nextPointId == null && unlocked) {
                    nextPointId = point.getId();
                    nextPointTitle = point.getTitle();
                }
            }
            vo.setStatus(status);
            pointVOs.add(vo);
        }

        LearningPathOverviewVO overview = new LearningPathOverviewVO();
        overview.setCourseId(courseId);
        overview.setTotalPoints(points.size());
        overview.setLearnedPoints(learnedSet.size());
        overview.setNextPointId(nextPointId);
        overview.setNextPointTitle(nextPointTitle);
        overview.setPoints(pointVOs);
        return overview;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markProgress(LearningPathProgressRequest request) {
        Long userId = requireUserId();
        if (request.getPointId() == null) {
            throw new IllegalArgumentException("知识点不能为空");
        }
        EduKnowledgePoint point = pointMapper.selectById(request.getPointId());
        if (point == null) {
            throw new IllegalArgumentException("知识点不存在");
        }
        if (request.getCourseId() != null && !request.getCourseId().equals(point.getCourseId())) {
            throw new IllegalArgumentException("知识点不属于该课程");
        }
        int status = request.getStatus() == null ? 1 : request.getStatus();
        if (status != 0 && status != 1) {
            throw new IllegalArgumentException("状态不合法");
        }

        if (status == 1) {
            List<EduKnowledgeDependency> pres = dependencyMapper.selectList(new LambdaQueryWrapper<EduKnowledgeDependency>()
                    .eq(EduKnowledgeDependency::getToPointId, point.getId()));
            if (!pres.isEmpty()) {
                List<Long> need = pres.stream().map(EduKnowledgeDependency::getFromPointId).collect(Collectors.toList());
                Set<Long> learned = progressMapper.selectList(new LambdaQueryWrapper<EduKnowledgeProgress>()
                                .eq(EduKnowledgeProgress::getUserId, userId)
                                .eq(EduKnowledgeProgress::getStatus, 1)
                                .in(EduKnowledgeProgress::getPointId, need))
                        .stream()
                        .map(EduKnowledgeProgress::getPointId)
                        .collect(Collectors.toSet());
                if (!learned.containsAll(need)) {
                    throw new IllegalArgumentException("请先完成前置知识点");
                }
            }
        }

        EduKnowledgeProgress progress = progressMapper.selectOne(new LambdaQueryWrapper<EduKnowledgeProgress>()
                .eq(EduKnowledgeProgress::getUserId, userId)
                .eq(EduKnowledgeProgress::getPointId, point.getId()));
        if (progress == null) {
            progress = new EduKnowledgeProgress();
            progress.setUserId(userId);
            progress.setPointId(point.getId());
            progress.setStatus(status);
            progress.setScore(request.getScore());
            progressMapper.insert(progress);
            return;
        }
        progress.setStatus(status);
        progress.setScore(request.getScore());
        progressMapper.updateById(progress);
    }
}
