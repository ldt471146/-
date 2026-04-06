package com.example.back.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * exam table bootstrap
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExamTableInitRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        createExamTaskTable();
        createExamTaskQuestionTable();
        createExamSubmissionTable();
    }

    private void createExamTaskTable() {
        jdbcTemplate.execute(String.join("\n",
                "CREATE TABLE IF NOT EXISTS edu_exam_task (",
                "    id BIGINT PRIMARY KEY AUTO_INCREMENT,",
                "    teacher_id BIGINT NOT NULL,",
                "    course_id BIGINT NOT NULL,",
                "    chapter_id BIGINT NULL,",
                "    title VARCHAR(200) NOT NULL,",
                "    question_count INT NOT NULL DEFAULT 10,",
                "    duration_minutes INT NOT NULL DEFAULT 30,",
                "    start_time DATETIME NOT NULL,",
                "    end_time DATETIME NULL,",
                "    status TINYINT NOT NULL DEFAULT 1,",
                "    created_at DATETIME NOT NULL,",
                "    updated_at DATETIME NOT NULL,",
                "    is_deleted TINYINT NOT NULL DEFAULT 0,",
                "    KEY idx_exam_task_teacher (teacher_id),",
                "    KEY idx_exam_task_course (course_id),",
                "    KEY idx_exam_task_status (status),",
                "    KEY idx_exam_task_start_time (start_time)",
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        ));
        log.info("exam task table checked");
    }

    private void createExamTaskQuestionTable() {
        jdbcTemplate.execute(String.join("\n",
                "CREATE TABLE IF NOT EXISTS edu_exam_task_question (",
                "    id BIGINT PRIMARY KEY AUTO_INCREMENT,",
                "    task_id BIGINT NOT NULL,",
                "    question_id BIGINT NOT NULL,",
                "    created_at DATETIME NOT NULL,",
                "    updated_at DATETIME NOT NULL,",
                "    is_deleted TINYINT NOT NULL DEFAULT 0,",
                "    KEY idx_exam_task_question_task (task_id),",
                "    KEY idx_exam_task_question_q (question_id),",
                "    UNIQUE KEY uk_exam_task_question (task_id, question_id)",
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        ));
        log.info("exam task question table checked");
    }

    private void createExamSubmissionTable() {
        jdbcTemplate.execute(String.join("\n",
                "CREATE TABLE IF NOT EXISTS edu_exam_submission (",
                "    id BIGINT PRIMARY KEY AUTO_INCREMENT,",
                "    task_id BIGINT NOT NULL,",
                "    user_id BIGINT NOT NULL,",
                "    total_count INT NOT NULL,",
                "    correct_count INT NOT NULL,",
                "    score INT NOT NULL,",
                "    detail_json LONGTEXT NULL,",
                "    submitted_at DATETIME NOT NULL,",
                "    created_at DATETIME NOT NULL,",
                "    updated_at DATETIME NOT NULL,",
                "    is_deleted TINYINT NOT NULL DEFAULT 0,",
                "    KEY idx_exam_submission_user (user_id),",
                "    KEY idx_exam_submission_task (task_id),",
                "    KEY idx_exam_submission_time (submitted_at)",
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        ));
        log.info("exam submission table checked");
    }
}