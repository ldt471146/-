package com.example.back.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 学习路径表初始化
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeTableInitRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        createPointTable();
        createDependencyTable();
        createProgressTable();
    }

    private void createPointTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS edu_knowledge_point (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    course_id BIGINT NOT NULL,
                    chapter_id BIGINT NULL,
                    title VARCHAR(200) NOT NULL,
                    description VARCHAR(500) NULL,
                    sort_no INT NOT NULL DEFAULT 0,
                    status TINYINT NOT NULL DEFAULT 1,
                    created_at DATETIME NOT NULL,
                    updated_at DATETIME NOT NULL,
                    is_deleted TINYINT NOT NULL DEFAULT 0,
                    KEY idx_knowledge_course (course_id),
                    KEY idx_knowledge_chapter (chapter_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
        log.info("知识点表检查完成");
    }

    private void createDependencyTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS edu_knowledge_dependency (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    from_point_id BIGINT NOT NULL,
                    to_point_id BIGINT NOT NULL,
                    created_at DATETIME NOT NULL,
                    updated_at DATETIME NOT NULL,
                    is_deleted TINYINT NOT NULL DEFAULT 0,
                    UNIQUE KEY uk_knowledge_dependency (from_point_id, to_point_id),
                    KEY idx_knowledge_to (to_point_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
        log.info("知识点依赖表检查完成");
    }

    private void createProgressTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS edu_knowledge_progress (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    user_id BIGINT NOT NULL,
                    point_id BIGINT NOT NULL,
                    status TINYINT NOT NULL DEFAULT 0,
                    score INT NULL,
                    created_at DATETIME NOT NULL,
                    updated_at DATETIME NOT NULL,
                    is_deleted TINYINT NOT NULL DEFAULT 0,
                    UNIQUE KEY uk_knowledge_progress (user_id, point_id),
                    KEY idx_knowledge_progress_point (point_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
        log.info("知识点进度表检查完成");
    }
}

