package com.example.back.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * knowledge table bootstrap
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeTableInitRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        createKnowledgePointTable();
        createKnowledgeDependencyTable();
        createKnowledgeProgressTable();
    }

    private void createKnowledgePointTable() {
        jdbcTemplate.execute(String.join("\n",
                "CREATE TABLE IF NOT EXISTS edu_knowledge_point (",
                "    id BIGINT PRIMARY KEY AUTO_INCREMENT,",
                "    course_id BIGINT NOT NULL,",
                "    chapter_id BIGINT NULL,",
                "    name VARCHAR(120) NOT NULL,",
                "    description VARCHAR(255) NULL,",
                "    sort_order INT NOT NULL DEFAULT 0,",
                "    created_at DATETIME NOT NULL,",
                "    updated_at DATETIME NOT NULL,",
                "    is_deleted TINYINT NOT NULL DEFAULT 0,",
                "    KEY idx_knowledge_course (course_id),",
                "    KEY idx_knowledge_chapter (chapter_id)",
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        ));
        log.info("edu_knowledge_point checked");
    }

    private void createKnowledgeDependencyTable() {
        jdbcTemplate.execute(String.join("\n",
                "CREATE TABLE IF NOT EXISTS edu_knowledge_dependency (",
                "    id BIGINT PRIMARY KEY AUTO_INCREMENT,",
                "    point_id BIGINT NOT NULL,",
                "    depends_on_id BIGINT NOT NULL,",
                "    created_at DATETIME NOT NULL,",
                "    updated_at DATETIME NOT NULL,",
                "    is_deleted TINYINT NOT NULL DEFAULT 0,",
                "    UNIQUE KEY uk_knowledge_dependency (point_id, depends_on_id)",
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        ));
        log.info("edu_knowledge_dependency checked");
    }

    private void createKnowledgeProgressTable() {
        jdbcTemplate.execute(String.join("\n",
                "CREATE TABLE IF NOT EXISTS edu_knowledge_progress (",
                "    id BIGINT PRIMARY KEY AUTO_INCREMENT,",
                "    user_id BIGINT NOT NULL,",
                "    point_id BIGINT NOT NULL,",
                "    status TINYINT NOT NULL DEFAULT 0,",
                "    score DECIMAL(5,2) NULL,",
                "    created_at DATETIME NOT NULL,",
                "    updated_at DATETIME NOT NULL,",
                "    is_deleted TINYINT NOT NULL DEFAULT 0,",
                "    UNIQUE KEY uk_knowledge_progress (user_id, point_id)",
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        ));
        log.info("edu_knowledge_progress checked");
    }
}