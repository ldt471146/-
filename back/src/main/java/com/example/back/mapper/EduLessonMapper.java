package com.example.back.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.back.entity.EduLesson;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EduLessonMapper extends BaseMapper<EduLesson> {

    @Select("""
            SELECT COUNT(1)
            FROM edu_lesson l
            JOIN edu_chapter c ON l.chapter_id = c.id
            WHERE c.course_id = #{courseId}
              AND l.is_deleted = 0
            """)
    Integer countByCourse(@Param("courseId") Long courseId);
}
