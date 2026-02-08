package com.example.back.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.back.dto.LearnProgressRequest;
import com.example.back.entity.EduLearnRecord;
import com.example.back.mapper.EduLearnRecordMapper;
import com.example.back.service.LearnService;
import com.example.back.util.SecurityUtil;
import com.example.back.vo.LearnRecordVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 学习记录服务实现
 */
@Service
public class LearnServiceImpl implements LearnService {

    private final EduLearnRecordMapper learnRecordMapper;

    public LearnServiceImpl(EduLearnRecordMapper learnRecordMapper) {
        this.learnRecordMapper = learnRecordMapper;
    }

    @Override
    public void updateProgress(LearnProgressRequest request) {
        Long userId = SecurityUtil.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        if (request.getProgress() == null) {
            throw new IllegalArgumentException("进度不能为空");
        }
        EduLearnRecord record = learnRecordMapper.selectOne(new LambdaQueryWrapper<EduLearnRecord>()
                .eq(EduLearnRecord::getUserId, userId)
                .eq(EduLearnRecord::getLessonId, request.getLessonId()));
        if (record == null) {
            record = new EduLearnRecord();
            record.setUserId(userId);
            record.setLessonId(request.getLessonId());
            int initProgress = Math.max(0, request.getProgress());
            record.setProgress(initProgress);
            record.setIsFinished(initProgress >= 100 ? 1 : 0);
            int add = request.getDurationSeconds() == null ? 0 : request.getDurationSeconds();
            record.setLearnSeconds(add);
            learnRecordMapper.insert(record);
        } else {
            int baseProgress = record.getProgress() == null ? 0 : record.getProgress();
            int newProgress = Math.max(baseProgress, request.getProgress());
            record.setProgress(newProgress);
            if (record.getIsFinished() != null && record.getIsFinished() == 1) {
                record.setIsFinished(1);
            } else {
                record.setIsFinished(newProgress >= 100 ? 1 : 0);
            }
            int base = record.getLearnSeconds() == null ? 0 : record.getLearnSeconds();
            int add = request.getDurationSeconds() == null ? 0 : request.getDurationSeconds();
            record.setLearnSeconds(base + add);
            learnRecordMapper.updateById(record);
        }
    }

    @Override
    public List<LearnRecordVO> listMyRecords() {
        Long userId = SecurityUtil.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        List<EduLearnRecord> records = learnRecordMapper.selectList(new LambdaQueryWrapper<EduLearnRecord>()
                .eq(EduLearnRecord::getUserId, userId));
        return records.stream().map(r -> {
            LearnRecordVO vo = new LearnRecordVO();
            vo.setLessonId(r.getLessonId());
            vo.setProgress(r.getProgress());
            vo.setIsFinished(r.getIsFinished());
            vo.setLearnSeconds(r.getLearnSeconds());
            return vo;
        }).collect(Collectors.toList());
    }
}
