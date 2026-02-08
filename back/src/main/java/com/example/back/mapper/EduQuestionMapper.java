package com.example.back.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.back.entity.EduQuestion;
import com.example.back.vo.QuestionMiniVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EduQuestionMapper extends BaseMapper<EduQuestion> {

    @Select("""
            SELECT q.id, q.title
            FROM edu_wrong_question w
            JOIN edu_question q ON q.id = w.question_id
            WHERE w.user_id = #{userId}
              AND w.is_deleted = 0
            ORDER BY w.updated_at DESC
            LIMIT #{limit}
            """)
    java.util.List<QuestionMiniVO> listRecentWrong(@Param("userId") Long userId, @Param("limit") int limit);
}
