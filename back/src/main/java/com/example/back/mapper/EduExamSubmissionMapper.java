package com.example.back.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.back.entity.EduExamSubmission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考试提交记录 Mapper
 */
@Mapper
public interface EduExamSubmissionMapper extends BaseMapper<EduExamSubmission> {
}

