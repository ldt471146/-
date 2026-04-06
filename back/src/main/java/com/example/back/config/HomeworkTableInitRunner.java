package com.example.back.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * homework table bootstrap
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
        jdbcTemplate.execute(String.join("\n",
                "CREATE TABLE IF NOT EXISTS edu_homework (",
                "    id BIGINT PRIMARY KEY AUTO_INCREMENT,",
                "    teacher_id BIGINT NOT NULL,",
                "    course_id BIGINT NOT NULL,",
                "    title VARCHAR(200) NOT NULL,",
                "    description TEXT NULL,",
                "    due_time DATETIME NULL,",
                "    total_score INT NOT NULL DEFAULT 100,",
                "    status TINYINT NOT NULL DEFAULT 1,",
                "    created_at DATETIME NOT NULL,",
                "    updated_at DATETIME NOT NULL,",
                "    is_deleted TINYINT NOT NULL DEFAULT 0,",
                "    KEY idx_homework_teacher (teacher_id),",
                "    KEY idx_homework_course (course_id)",
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        ));
        log.info("edu_homework checked");
    }

    private void createHomeworkProblemTable() {
        jdbcTemplate.execute(String.join("\n",
                "CREATE TABLE IF NOT EXISTS edu_homework_problem (",
                "    id BIGINT PRIMARY KEY AUTO_INCREMENT,",
                "    homework_id BIGINT NOT NULL,",
                "    problem_id BIGINT NOT NULL,",
                "    score INT NOT NULL DEFAULT 0,",
                "    sort_order INT NOT NULL DEFAULT 0,",
                "    created_at DATETIME NOT NULL,",
                "    updated_at DATETIME NOT NULL,",
                "    is_deleted TINYINT NOT NULL DEFAULT 0,",
                "    UNIQUE KEY uk_hw_problem (homework_id, problem_id)",
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        ));
        log.info("edu_homework_problem checked");
    }
}