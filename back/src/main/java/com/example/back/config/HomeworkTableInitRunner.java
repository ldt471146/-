package com.example.back.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 作业表初始化
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HomeworkTableInitRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        createHomeworkTable();
        createHomeworkProblemTable();
    }

    private void createHomeworkTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS edu_homework (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    course_id BIGINT NOT NULL,
                    title VARCHAR(128) NOT NULL,
                    deadline DATETIME NULL,
                    created_at DATETIME NOT NULL,
                    updated_at DATETIME NOT NULL,
                    is_deleted TINYINT NOT NULL DEFAULT 0,
                    KEY idx_homework_course (course_id),
                    KEY idx_homework_deadline (deadline)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
        log.info("作业表检查完成");
    }

    private void createHomeworkProblemTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS edu_homework_problem (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    homework_id BIGINT NOT NULL,
                    problem_id BIGINT NOT NULL,
                    score INT NOT NULL DEFAULT 100,
                    created_at DATETIME NOT NULL,
                    updated_at DATETIME NOT NULL,
                    is_deleted TINYINT NOT NULL DEFAULT 0,
                    UNIQUE KEY uk_hw_problem (homework_id, problem_id),
                    KEY idx_hw_problem_homework (homework_id),
                    KEY idx_hw_problem_problem (problem_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
        log.info("作业题目关联表检查完成");
    }
}

