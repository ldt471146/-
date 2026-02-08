package com.example.back.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ReportMapper {

    @Select("SELECT COUNT(1) FROM edu_course WHERE status = 1 AND is_deleted = 0")
    Integer countAllCourses();

    @Select("""
            SELECT COUNT(1)
            FROM edu_course_enroll
            WHERE user_id = #{userId}
              AND status = 1
              AND is_deleted = 0
            """)
    Integer countMyCourses(@Param("userId") Long userId);

    @Select("""
            SELECT COUNT(1)
            FROM edu_lesson l
            JOIN edu_chapter c ON l.chapter_id = c.id
            JOIN edu_course_enroll e ON e.course_id = c.course_id
            WHERE e.user_id = #{userId}
              AND e.status = 1
              AND l.is_deleted = 0
              AND c.is_deleted = 0
              AND e.is_deleted = 0
            """)
    Integer countTotalLessons(@Param("userId") Long userId);

    @Select("""
            SELECT COUNT(1)
            FROM edu_learn_record r
            WHERE r.user_id = #{userId}
              AND r.is_finished = 1
              AND r.is_deleted = 0
            """)
    Integer countFinishedLessons(@Param("userId") Long userId);

    @Select("""
            SELECT COALESCE(SUM(learn_seconds), 0)
            FROM edu_learn_record
            WHERE user_id = #{userId}
              AND is_deleted = 0
            """)
    Integer sumLearnSeconds(@Param("userId") Long userId);

    @Select("""
            SELECT COUNT(1)
            FROM edu_question_record
            WHERE user_id = #{userId}
              AND is_deleted = 0
            """)
    Integer countQuestionTotal(@Param("userId") Long userId);

    @Select("""
            SELECT COUNT(1)
            FROM edu_question_record
            WHERE user_id = #{userId}
              AND is_correct = 1
              AND is_deleted = 0
            """)
    Integer countQuestionCorrect(@Param("userId") Long userId);

    @Select("""
            SELECT COUNT(1)
            FROM edu_wrong_question
            WHERE user_id = #{userId}
              AND is_deleted = 0
            """)
    Integer countWrong(@Param("userId") Long userId);

    @Select("""
            SELECT COUNT(DISTINCT question_id)
            FROM edu_question_record
            WHERE user_id = #{userId}
              AND is_correct = 1
              AND question_id IN (
                SELECT DISTINCT question_id
                FROM edu_question_record
                WHERE user_id = #{userId} AND is_correct = 0
              )
            """)
    Integer countWrongRedo(@Param("userId") Long userId);

    @Select("""
            SELECT COUNT(1)
            FROM edu_question_favorite
            WHERE user_id = #{userId}
              AND is_deleted = 0
            """)
    Integer countFavorites(@Param("userId") Long userId);

    @Select("""
            SELECT DATE_FORMAT(created_at, '%m-%d') AS day,
                   COUNT(1) AS total,
                   SUM(CASE WHEN is_correct = 1 THEN 1 ELSE 0 END) AS correct
            FROM edu_question_record
            WHERE user_id = #{userId}
              AND created_at >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)
            GROUP BY DATE_FORMAT(created_at, '%m-%d')
            ORDER BY MIN(created_at)
            """)
    java.util.List<com.example.back.vo.ReportQuestionTrendVO> questionTrend(@Param("userId") Long userId);

    @Select("""
            SELECT DATE_FORMAT(updated_at, '%m-%d') AS day,
                   COALESCE(SUM(learn_seconds), 0) AS learnSeconds
            FROM edu_learn_record
            WHERE user_id = #{userId}
              AND updated_at >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)
              AND is_deleted = 0
            GROUP BY DATE_FORMAT(updated_at, '%m-%d')
            ORDER BY MIN(updated_at)
            """)
    java.util.List<com.example.back.vo.ReportLearnTrendVO> learnTrend(@Param("userId") Long userId);

    @Select("""
            SELECT q.course_id AS id,
                   c.title AS name,
                   COALESCE(SUM(w.wrong_count), 0) AS value
            FROM edu_wrong_question w
            JOIN edu_question q ON w.question_id = q.id
            JOIN edu_course c ON q.course_id = c.id
            WHERE w.user_id = #{userId}
              AND w.is_deleted = 0
              AND c.is_deleted = 0
            GROUP BY q.course_id, c.title
            ORDER BY value DESC
            LIMIT 5
            """)
    java.util.List<com.example.back.vo.ReportWeakTagVO> listWeakCourses(@Param("userId") Long userId);
}
