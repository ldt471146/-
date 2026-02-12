package com.example.back.mapper;

import com.example.back.vo.TeacherCourseStatVO;
import com.example.back.vo.TeacherExamStatVO;
import com.example.back.vo.TeacherStudentRankVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TeacherStatsMapper {

    @Select("""
            SELECT COUNT(1)
            FROM edu_course
            WHERE teacher_id = #{teacherId}
              AND is_deleted = 0
            """)
    Integer countCourses(@Param("teacherId") Long teacherId);

    @Select("""
            SELECT COUNT(DISTINCT ce.user_id)
            FROM edu_course_enroll ce
            JOIN edu_course c ON ce.course_id = c.id
            WHERE c.teacher_id = #{teacherId}
              AND ce.status = 1
              AND ce.is_deleted = 0
              AND c.is_deleted = 0
            """)
    Integer countStudents(@Param("teacherId") Long teacherId);

    @Select("""
            SELECT COUNT(1)
            FROM edu_exam_submission es
            JOIN edu_exam_task et ON es.task_id = et.id
            WHERE et.teacher_id = #{teacherId}
              AND es.is_deleted = 0
              AND et.is_deleted = 0
            """)
    Integer countSubmissions(@Param("teacherId") Long teacherId);

    @Select("""
            SELECT COALESCE(ROUND(AVG(es.score), 1), 0)
            FROM edu_exam_submission es
            JOIN edu_exam_task et ON es.task_id = et.id
            WHERE et.teacher_id = #{teacherId}
              AND es.is_deleted = 0
              AND et.is_deleted = 0
            """)
    Double avgScore(@Param("teacherId") Long teacherId);

    @Select("""
            SELECT u.id AS userId,
                   u.username AS username,
                   COALESCE(ls.learn_minutes, 0) AS learnMinutes,
                   COALESCE(es.avg_score, 0) AS avgScore,
                   COALESCE(es.submission_count, 0) AS submissionCount
            FROM (
                SELECT DISTINCT ce.user_id
                FROM edu_course_enroll ce
                JOIN edu_course c ON ce.course_id = c.id
                WHERE c.teacher_id = #{teacherId}
                  AND ce.status = 1
                  AND ce.is_deleted = 0
                  AND c.is_deleted = 0
            ) su
            JOIN sys_user u ON u.id = su.user_id AND u.is_deleted = 0
            LEFT JOIN (
                SELECT lr.user_id,
                       ROUND(SUM(lr.learn_seconds) / 60) AS learn_minutes
                FROM edu_learn_record lr
                JOIN edu_lesson l ON lr.lesson_id = l.id
                JOIN edu_chapter ch ON l.chapter_id = ch.id
                JOIN edu_course c ON ch.course_id = c.id
                WHERE c.teacher_id = #{teacherId}
                  AND lr.is_deleted = 0
                  AND l.is_deleted = 0
                  AND ch.is_deleted = 0
                  AND c.is_deleted = 0
                GROUP BY lr.user_id
            ) ls ON ls.user_id = u.id
            LEFT JOIN (
                SELECT es.user_id,
                       ROUND(AVG(es.score), 1) AS avg_score,
                       COUNT(1) AS submission_count
                FROM edu_exam_submission es
                JOIN edu_exam_task et ON es.task_id = et.id
                WHERE et.teacher_id = #{teacherId}
                  AND es.is_deleted = 0
                  AND et.is_deleted = 0
                GROUP BY es.user_id
            ) es ON es.user_id = u.id
            ORDER BY learnMinutes DESC, avgScore DESC, submissionCount DESC
            LIMIT 20
            """)
    List<TeacherStudentRankVO> studentRanks(@Param("teacherId") Long teacherId);

    @Select("""
            SELECT c.id AS courseId,
                   c.title AS courseTitle,
                   COALESCE(st.student_count, 0) AS studentCount,
                   COALESCE(ex.avg_score, 0) AS avgScore,
                   COALESCE(ls.total_learn_minutes, 0) AS totalLearnMinutes
            FROM edu_course c
            LEFT JOIN (
                SELECT course_id, COUNT(DISTINCT user_id) AS student_count
                FROM edu_course_enroll
                WHERE status = 1
                  AND is_deleted = 0
                GROUP BY course_id
            ) st ON st.course_id = c.id
            LEFT JOIN (
                SELECT et.course_id,
                       ROUND(AVG(es.score), 1) AS avg_score
                FROM edu_exam_task et
                LEFT JOIN edu_exam_submission es ON es.task_id = et.id AND es.is_deleted = 0
                WHERE et.teacher_id = #{teacherId}
                  AND et.is_deleted = 0
                GROUP BY et.course_id
            ) ex ON ex.course_id = c.id
            LEFT JOIN (
                SELECT ch.course_id,
                       ROUND(SUM(lr.learn_seconds) / 60) AS total_learn_minutes
                FROM edu_learn_record lr
                JOIN edu_lesson l ON lr.lesson_id = l.id
                JOIN edu_chapter ch ON l.chapter_id = ch.id
                JOIN edu_course c2 ON ch.course_id = c2.id
                WHERE c2.teacher_id = #{teacherId}
                  AND lr.is_deleted = 0
                  AND l.is_deleted = 0
                  AND ch.is_deleted = 0
                  AND c2.is_deleted = 0
                GROUP BY ch.course_id
            ) ls ON ls.course_id = c.id
            WHERE c.teacher_id = #{teacherId}
              AND c.is_deleted = 0
            ORDER BY c.id DESC
            """)
    List<TeacherCourseStatVO> courseStats(@Param("teacherId") Long teacherId);

    @Select("""
            SELECT et.id AS taskId,
                   et.title AS taskTitle,
                   COUNT(es.id) AS attempts,
                   COALESCE(SUM(CASE WHEN es.score >= 60 THEN 1 ELSE 0 END), 0) AS passCount,
                   CASE
                     WHEN COUNT(es.id) = 0 THEN 0
                     ELSE ROUND(SUM(CASE WHEN es.score >= 60 THEN 1 ELSE 0 END) * 100.0 / COUNT(es.id))
                   END AS passRate
            FROM edu_exam_task et
            LEFT JOIN edu_exam_submission es ON es.task_id = et.id AND es.is_deleted = 0
            WHERE et.teacher_id = #{teacherId}
              AND et.is_deleted = 0
            GROUP BY et.id, et.title
            ORDER BY et.id DESC
            LIMIT 30
            """)
    List<TeacherExamStatVO> examStats(@Param("teacherId") Long teacherId);
}

