package com.example.back.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.back.dto.QuestionSubmitRequest;
import com.example.back.entity.EduQuestion;
import com.example.back.entity.EduQuestionOption;
import com.example.back.entity.EduQuestionRecord;
import com.example.back.entity.EduQuestionFavorite;
import com.example.back.entity.EduWrongQuestion;
import com.example.back.mapper.EduQuestionMapper;
import com.example.back.mapper.EduQuestionOptionMapper;
import com.example.back.mapper.EduQuestionRecordMapper;
import com.example.back.mapper.EduQuestionFavoriteMapper;
import com.example.back.mapper.EduWrongQuestionMapper;
import com.example.back.service.QuestionService;
import com.example.back.util.SecurityUtil;
import com.example.back.vo.QuestionOptionVO;
import com.example.back.vo.QuestionStatsVO;
import com.example.back.vo.QuestionSubmitResultVO;
import com.example.back.vo.QuestionVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 题库服务实现
 */
@Service
public class QuestionServiceImpl implements QuestionService {

    private final EduQuestionMapper questionMapper;
    private final EduQuestionOptionMapper optionMapper;
    private final EduQuestionRecordMapper recordMapper;
    private final EduQuestionFavoriteMapper favoriteMapper;
    private final EduWrongQuestionMapper wrongMapper;

    public QuestionServiceImpl(EduQuestionMapper questionMapper,
                               EduQuestionOptionMapper optionMapper,
                               EduQuestionRecordMapper recordMapper,
                               EduQuestionFavoriteMapper favoriteMapper,
                               EduWrongQuestionMapper wrongMapper) {
        this.questionMapper = questionMapper;
        this.optionMapper = optionMapper;
        this.recordMapper = recordMapper;
        this.favoriteMapper = favoriteMapper;
        this.wrongMapper = wrongMapper;
    }

    @Override
    public List<QuestionVO> listQuestions(Long courseId) {
        return listQuestionsPage(courseId, null, null, null, 1, 1000).getRecords();
    }

    @Override
    public com.example.back.vo.PageResultVO<QuestionVO> listQuestionsPage(Long courseId, Long chapterId, String type, Integer difficulty, long page, long size) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<EduQuestion> mpPage =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<EduQuestion> result =
                questionMapper.selectPage(mpPage, new LambdaQueryWrapper<EduQuestion>()
                        .eq(courseId != null, EduQuestion::getCourseId, courseId)
                        .eq(chapterId != null, EduQuestion::getChapterId, chapterId)
                        .eq(type != null && !type.isBlank(), EduQuestion::getType, type)
                        .eq(difficulty != null, EduQuestion::getDifficulty, difficulty)
                        .orderByDesc(EduQuestion::getId));

        List<EduQuestion> questions = result.getRecords();
        List<QuestionVO> vos = buildQuestionVOs(questions);

        com.example.back.vo.PageResultVO<QuestionVO> pageResult = new com.example.back.vo.PageResultVO<>();
        pageResult.setPage(page);
        pageResult.setSize(size);
        pageResult.setTotal(result.getTotal());
        pageResult.setRecords(vos);
        return pageResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuestionSubmitResultVO submit(QuestionSubmitRequest request) {
        Long userId = SecurityUtil.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        EduQuestion question = questionMapper.selectById(request.getQuestionId());
        if (question == null) {
            throw new IllegalArgumentException("题目不存在");
        }

        List<EduQuestionOption> options = optionMapper.selectList(new LambdaQueryWrapper<EduQuestionOption>()
                .eq(EduQuestionOption::getQuestionId, request.getQuestionId()));
        List<String> correct = options.stream()
                .filter(o -> o.getIsCorrect() != null && o.getIsCorrect() == 1)
                .sorted(Comparator.comparing(EduQuestionOption::getLabel))
                .map(EduQuestionOption::getLabel)
                .collect(Collectors.toList());

        List<String> answers = request.getAnswers() == null ? List.of() : request.getAnswers();
        List<String> normAnswers = answers.stream().sorted().collect(Collectors.toList());

        boolean ok = correct.equals(normAnswers);

        EduQuestionRecord record = new EduQuestionRecord();
        record.setUserId(userId);
        record.setQuestionId(request.getQuestionId());
        record.setAnswer(String.join(",", normAnswers));
        record.setIsCorrect(ok ? 1 : 0);
        recordMapper.insert(record);

        if (!ok) {
            EduWrongQuestion wrong = wrongMapper.selectOne(new LambdaQueryWrapper<EduWrongQuestion>()
                    .eq(EduWrongQuestion::getUserId, userId)
                    .eq(EduWrongQuestion::getQuestionId, request.getQuestionId()));
            if (wrong == null) {
                wrong = new EduWrongQuestion();
                wrong.setUserId(userId);
                wrong.setQuestionId(request.getQuestionId());
                wrong.setWrongCount(1);
                wrongMapper.insert(wrong);
            } else {
                wrong.setWrongCount((wrong.getWrongCount() == null ? 0 : wrong.getWrongCount()) + 1);
                wrongMapper.updateById(wrong);
            }
        } else {
            wrongMapper.delete(new LambdaQueryWrapper<EduWrongQuestion>()
                    .eq(EduWrongQuestion::getUserId, userId)
                    .eq(EduWrongQuestion::getQuestionId, request.getQuestionId()));
        }

        QuestionSubmitResultVO result = new QuestionSubmitResultVO();
        result.setQuestionId(request.getQuestionId());
        result.setCorrect(ok);
        result.setCorrectAnswers(correct);
        result.setAnalysis(question.getAnalysis());
        return result;
    }

    @Override
    public com.example.back.vo.PageResultVO<QuestionVO> listWrongQuestionsPage(Long courseId, Long chapterId, long page, long size) {
        Long userId = SecurityUtil.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        List<Long> allowedIds = null;
        if (courseId != null || chapterId != null) {
            allowedIds = questionMapper.selectList(new LambdaQueryWrapper<EduQuestion>()
                            .eq(courseId != null, EduQuestion::getCourseId, courseId)
                            .eq(chapterId != null, EduQuestion::getChapterId, chapterId))
                    .stream()
                    .map(EduQuestion::getId)
                    .collect(Collectors.toList());
            if (allowedIds.isEmpty()) {
                com.example.back.vo.PageResultVO<QuestionVO> empty = new com.example.back.vo.PageResultVO<>();
                empty.setPage(page);
                empty.setSize(size);
                empty.setTotal(0);
                empty.setRecords(List.of());
                return empty;
            }
        }
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<EduWrongQuestion> mpPage =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<EduWrongQuestion> result =
                wrongMapper.selectPage(mpPage, new LambdaQueryWrapper<EduWrongQuestion>()
                        .eq(EduWrongQuestion::getUserId, userId)
                        .in(allowedIds != null, EduWrongQuestion::getQuestionId, allowedIds)
                        .orderByDesc(EduWrongQuestion::getUpdatedAt));
        List<EduWrongQuestion> wrongs = result.getRecords();
        List<Long> ids = wrongs.stream().map(EduWrongQuestion::getQuestionId).collect(Collectors.toList());
        List<QuestionVO> vos = buildQuestionVOsByIds(ids);

        com.example.back.vo.PageResultVO<QuestionVO> pageResult = new com.example.back.vo.PageResultVO<>();
        pageResult.setPage(page);
        pageResult.setSize(size);
        pageResult.setTotal(result.getTotal());
        pageResult.setRecords(vos);
        return pageResult;
    }

    @Override
    public com.example.back.vo.PageResultVO<QuestionVO> listFavoriteQuestionsPage(Long courseId, Long chapterId, long page, long size) {
        Long userId = SecurityUtil.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        List<Long> allowedIds = null;
        if (courseId != null || chapterId != null) {
            allowedIds = questionMapper.selectList(new LambdaQueryWrapper<EduQuestion>()
                            .eq(courseId != null, EduQuestion::getCourseId, courseId)
                            .eq(chapterId != null, EduQuestion::getChapterId, chapterId))
                    .stream()
                    .map(EduQuestion::getId)
                    .collect(Collectors.toList());
            if (allowedIds.isEmpty()) {
                com.example.back.vo.PageResultVO<QuestionVO> empty = new com.example.back.vo.PageResultVO<>();
                empty.setPage(page);
                empty.setSize(size);
                empty.setTotal(0);
                empty.setRecords(List.of());
                return empty;
            }
        }
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<EduQuestionFavorite> mpPage =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<EduQuestionFavorite> result =
                favoriteMapper.selectPage(mpPage, new LambdaQueryWrapper<EduQuestionFavorite>()
                        .eq(EduQuestionFavorite::getUserId, userId)
                        .in(allowedIds != null, EduQuestionFavorite::getQuestionId, allowedIds)
                        .orderByDesc(EduQuestionFavorite::getUpdatedAt));
        List<Long> ids = result.getRecords().stream().map(EduQuestionFavorite::getQuestionId).collect(Collectors.toList());
        List<QuestionVO> vos = buildQuestionVOsByIds(ids);

        com.example.back.vo.PageResultVO<QuestionVO> pageResult = new com.example.back.vo.PageResultVO<>();
        pageResult.setPage(page);
        pageResult.setSize(size);
        pageResult.setTotal(result.getTotal());
        pageResult.setRecords(vos);
        return pageResult;
    }

    @Override
    public List<Long> listFavoriteIds() {
        Long userId = SecurityUtil.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        return favoriteMapper.selectList(new LambdaQueryWrapper<EduQuestionFavorite>()
                .eq(EduQuestionFavorite::getUserId, userId))
                .stream()
                .map(EduQuestionFavorite::getQuestionId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void favorite(Long questionId) {
        Long userId = SecurityUtil.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        EduQuestionFavorite existing = favoriteMapper.selectOne(new LambdaQueryWrapper<EduQuestionFavorite>()
                .eq(EduQuestionFavorite::getUserId, userId)
                .eq(EduQuestionFavorite::getQuestionId, questionId));
        if (existing != null) {
            return;
        }
        EduQuestionFavorite fav = new EduQuestionFavorite();
        fav.setUserId(userId);
        fav.setQuestionId(questionId);
        fav.setIsDeleted(0);
        favoriteMapper.insert(fav);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelFavorite(Long questionId) {
        Long userId = SecurityUtil.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        favoriteMapper.delete(new LambdaQueryWrapper<EduQuestionFavorite>()
                .eq(EduQuestionFavorite::getUserId, userId)
                .eq(EduQuestionFavorite::getQuestionId, questionId));
    }

    @Override
    public QuestionStatsVO stats() {
        Long userId = SecurityUtil.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        Long totalObj = recordMapper.selectCount(new LambdaQueryWrapper<EduQuestionRecord>()
                .eq(EduQuestionRecord::getUserId, userId));
        Long correctObj = recordMapper.selectCount(new LambdaQueryWrapper<EduQuestionRecord>()
                .eq(EduQuestionRecord::getUserId, userId)
                .eq(EduQuestionRecord::getIsCorrect, 1));
        Long wrongObj = wrongMapper.selectCount(new LambdaQueryWrapper<EduWrongQuestion>()
                .eq(EduWrongQuestion::getUserId, userId));
        Long favObj = favoriteMapper.selectCount(new LambdaQueryWrapper<EduQuestionFavorite>()
                .eq(EduQuestionFavorite::getUserId, userId));

        int total = totalObj == null ? 0 : totalObj.intValue();
        int correct = correctObj == null ? 0 : correctObj.intValue();
        int wrongCount = wrongObj == null ? 0 : wrongObj.intValue();
        int favoriteCount = favObj == null ? 0 : favObj.intValue();

        Long wrongRedoObj = recordMapper.selectCount(new LambdaQueryWrapper<EduQuestionRecord>()
                .eq(EduQuestionRecord::getUserId, userId)
                .eq(EduQuestionRecord::getIsCorrect, 1)
                .inSql(EduQuestionRecord::getQuestionId,
                        "SELECT DISTINCT question_id FROM edu_question_record WHERE user_id = " + userId + " AND is_correct = 0"));
        int wrongRedo = wrongRedoObj == null ? 0 : wrongRedoObj.intValue();

        QuestionStatsVO vo = new QuestionStatsVO();
        vo.setTotal(total);
        vo.setCorrect(correct);
        vo.setAccuracy(total == 0 ? 0 : (int) Math.round(correct * 100.0 / total));
        vo.setWrongCount(wrongCount);
        vo.setFavoriteCount(favoriteCount);
        vo.setWrongRedoCount(wrongRedo);
        vo.setRecentWrong(questionMapper.listRecentWrong(userId, 5));
        return vo;
    }

    private List<QuestionVO> buildQuestionVOs(List<EduQuestion> questions) {
        if (questions == null || questions.isEmpty()) {
            return List.of();
        }
        List<Long> ids = questions.stream().map(EduQuestion::getId).collect(Collectors.toList());
        List<EduQuestionOption> options = optionMapper.selectList(new LambdaQueryWrapper<EduQuestionOption>()
                .in(EduQuestionOption::getQuestionId, ids)
                .orderByAsc(EduQuestionOption::getLabel));
        Map<Long, List<EduQuestionOption>> optionMap = options.stream()
                .collect(Collectors.groupingBy(EduQuestionOption::getQuestionId));
        return questions.stream().map(q -> toQuestionVO(q, optionMap.getOrDefault(q.getId(), List.of())))
                .collect(Collectors.toList());
    }

    private List<QuestionVO> buildQuestionVOsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<EduQuestion> questions = questionMapper.selectList(new LambdaQueryWrapper<EduQuestion>()
                .in(EduQuestion::getId, ids));
        List<EduQuestionOption> options = optionMapper.selectList(new LambdaQueryWrapper<EduQuestionOption>()
                .in(EduQuestionOption::getQuestionId, ids)
                .orderByAsc(EduQuestionOption::getLabel));
        Map<Long, List<EduQuestionOption>> optionMap = options.stream()
                .collect(Collectors.groupingBy(EduQuestionOption::getQuestionId));
        Map<Long, QuestionVO> voMap = questions.stream()
                .map(q -> toQuestionVO(q, optionMap.getOrDefault(q.getId(), List.of())))
                .collect(Collectors.toMap(QuestionVO::getId, v -> v));
        return ids.stream().map(voMap::get).filter(v -> v != null).collect(Collectors.toList());
    }

    private QuestionVO toQuestionVO(EduQuestion question, List<EduQuestionOption> options) {
        QuestionVO vo = new QuestionVO();
        vo.setId(question.getId());
        vo.setTitle(question.getTitle());
        vo.setType(question.getType());
        vo.setDifficulty(question.getDifficulty());
        vo.setCourseId(question.getCourseId());
        vo.setChapterId(question.getChapterId());
        vo.setOptions(options.stream().map(opt -> {
            QuestionOptionVO o = new QuestionOptionVO();
            o.setLabel(opt.getLabel());
            o.setContent(opt.getContent());
            return o;
        }).collect(Collectors.toList()));
        return vo;
    }
}
