package com.example.back.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.back.entity.EduCourse;
import com.example.back.vo.CourseVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EduCourseMapper extends BaseMapper<EduCourse> {

    @Select("""
            SELECT c.id, c.title, c.cover, c.intro
            FROM edu_course c
            JOIN edu_course_enroll e ON e.course_id = c.id
            WHERE e.user_id = #{userId}
              AND e.status = 1
              AND c.status = 1
              AND c.is_deleted = 0
              AND e.is_deleted = 0
            ORDER BY e.created_at DESC
            """)
    java.util.List<CourseVO> selectMyCourses(@Param("userId") Long userId);
}
