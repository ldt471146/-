package com.example.back.service.impl;

import com.example.back.mapper.ReportMapper;
import com.example.back.service.ReportService;
import com.example.back.util.SecurityUtil;
import com.example.back.vo.ReportOverviewVO;
import org.springframework.stereotype.Service;

/**
 * 成长报告服务实现
 */
@Service
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;

    public ReportServiceImpl(ReportMapper reportMapper) {
        this.reportMapper = reportMapper;
    }

    @Override
    public ReportOverviewVO overview() {
        Long userId = SecurityUtil.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        ReportOverviewVO vo = new ReportOverviewVO();
        Integer totalCourses = reportMapper.countAllCourses();
        Integer myCourses = reportMapper.countMyCourses(userId);
        Integer totalLessons = reportMapper.countTotalLessons(userId);
        Integer finishedLessons = reportMapper.countFinishedLessons(userId);
        Integer learnSeconds = reportMapper.sumLearnSeconds(userId);
        Integer questionTotal = reportMapper.countQuestionTotal(userId);
        Integer questionCorrect = reportMapper.countQuestionCorrect(userId);
        Integer wrongCount = reportMapper.countWrong(userId);
        Integer wrongRedo = reportMapper.countWrongRedo(userId);
        Integer favoriteCount = reportMapper.countFavorites(userId);

        int totalC = totalCourses == null ? 0 : totalCourses;
        int myC = myCourses == null ? 0 : myCourses;
        int totalL = totalLessons == null ? 0 : totalLessons;
        int finishedL = finishedLessons == null ? 0 : finishedLessons;
        int learnS = learnSeconds == null ? 0 : learnSeconds;
        int qTotal = questionTotal == null ? 0 : questionTotal;
        int qCorrect = questionCorrect == null ? 0 : questionCorrect;
        int wrong = wrongCount == null ? 0 : wrongCount;
        int redo = wrongRedo == null ? 0 : wrongRedo;
        int fav = favoriteCount == null ? 0 : favoriteCount;

        vo.setTotalCourses(totalC);
        vo.setMyCourses(myC);
        vo.setTotalLessons(totalL);
        vo.setFinishedLessons(finishedL);
        vo.setLearnSeconds(learnS);
        vo.setQuestionTotal(qTotal);
        vo.setQuestionCorrect(qCorrect);
        vo.setWrongCount(wrong);
        vo.setWrongRedoCount(redo);
        vo.setFavoriteCount(fav);
        vo.setQuestionAccuracy(qTotal == 0 ? 0 : (int) Math.round(qCorrect * 100.0 / qTotal));
        vo.setWeakCourses(reportMapper.listWeakCourses(userId));
        return vo;
    }

    @Override
    public com.example.back.vo.ReportTrendVO trend() {
        Long userId = SecurityUtil.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        java.util.List<com.example.back.vo.ReportLearnTrendVO> learn = reportMapper.learnTrend(userId);
        java.util.List<com.example.back.vo.ReportQuestionTrendVO> question = reportMapper.questionTrend(userId);

        java.util.Map<String, Integer> learnMap = new java.util.HashMap<>();
        for (com.example.back.vo.ReportLearnTrendVO l : learn) {
            learnMap.put(l.getDay(), l.getLearnSeconds() == null ? 0 : l.getLearnSeconds());
        }
        java.util.Map<String, com.example.back.vo.ReportQuestionTrendVO> qMap = new java.util.HashMap<>();
        for (com.example.back.vo.ReportQuestionTrendVO q : question) {
            qMap.put(q.getDay(), q);
        }

        java.util.List<String> days = new java.util.ArrayList<>();
        java.util.List<Integer> learnMinutes = new java.util.ArrayList<>();
        java.util.List<Integer> qTotal = new java.util.ArrayList<>();
        java.util.List<Integer> qCorrect = new java.util.ArrayList<>();

        java.time.LocalDate start = java.time.LocalDate.now().minusDays(6);
        for (int i = 0; i < 7; i++) {
            java.time.LocalDate d = start.plusDays(i);
            String key = d.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd"));
            days.add(key);
            int sec = learnMap.getOrDefault(key, 0);
            learnMinutes.add((int) Math.round(sec / 60.0));
            com.example.back.vo.ReportQuestionTrendVO qv = qMap.get(key);
            qTotal.add(qv == null || qv.getTotal() == null ? 0 : qv.getTotal());
            qCorrect.add(qv == null || qv.getCorrect() == null ? 0 : qv.getCorrect());
        }

        com.example.back.vo.ReportTrendVO vo = new com.example.back.vo.ReportTrendVO();
        vo.setDays(days);
        vo.setLearnMinutes(learnMinutes);
        vo.setQuestionTotal(qTotal);
        vo.setQuestionCorrect(qCorrect);
        return vo;
    }
}
