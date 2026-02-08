package com.example.back.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.back.entity.EduLearnRecord;
import com.example.back.vo.CourseLastLearnVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EduLearnRecordMapper extends BaseMapper<EduLearnRecord> {

    @Select("""
            SELECT COUNT(1)
            FROM edu_learn_record r
            JOIN edu_lesson l ON r.lesson_id = l.id
            JOIN edu_chapter c ON l.chapter_id = c.id
            WHERE r.user_id = #{userId}
              AND c.course_id = #{courseId}
              AND r.is_finished = 1
              AND r.is_deleted = 0
            """)
    Integer countFinishedByCourse(@Param("userId") Long userId, @Param("courseId") Long courseId);

    @Select("""
            SELECT c.course_id AS courseId,
                   r.lesson_id AS lessonId,
                   l.title AS lessonTitle,
                   r.updated_at AS learnedAt
            FROM edu_learn_record r
            JOIN edu_lesson l ON r.lesson_id = l.id
            JOIN edu_chapter c ON l.chapter_id = c.id
            JOIN (
                SELECT c2.course_id AS course_id, MAX(r2.updated_at) AS max_time
                FROM edu_learn_record r2
                JOIN edu_lesson l2 ON r2.lesson_id = l2.id
                JOIN edu_chapter c2 ON l2.chapter_id = c2.id
                WHERE r2.user_id = #{userId}
                  AND r2.is_deleted = 0
                GROUP BY c2.course_id
            ) t ON t.course_id = c.course_id AND r.updated_at = t.max_time
            WHERE r.user_id = #{userId}
              AND r.is_deleted = 0
            """)
    java.util.List<CourseLastLearnVO> listLastLearnByUser(@Param("userId") Long userId);
}
