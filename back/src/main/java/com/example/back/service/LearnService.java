package com.example.back.service;

import com.example.back.dto.LearnProgressRequest;
import com.example.back.vo.LearnRecordVO;

import java.util.List;

/**
 * 学习记录服务
 */
public interface LearnService {

    void updateProgress(LearnProgressRequest request);

    List<LearnRecordVO> listMyRecords();
}
