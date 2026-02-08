package com.example.back.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.back.entity.EduExamTaskQuestion;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考试任务题目 Mapper
 */
@Mapper
public interface EduExamTaskQuestionMapper extends BaseMapper<EduExamTaskQuestion> {
}

