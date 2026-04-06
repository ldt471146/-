package com.example.back.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * governance table bootstrap
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GovernanceTableInitRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        ensureSysUserGovernanceColumns();
        createCommunityPostTable();
        createCommunityReplyTable();
        createCommunityModerationTable();
        createAuditLogTable();
    }

    private void ensureSysUserGovernanceColumns() {
        try {
            if (!hasColumn("sys_user", "mute_status")) {
                jdbcTemplate.execute("ALTER TABLE sys_user ADD COLUMN mute_status TINYINT NOT NULL DEFAULT 0");
                log.info("sys_user.mute_status added");
            }
            if (!hasColumn("sys_user", "ban_reason")) {
                jdbcTemplate.execute("ALTER TABLE sys_user ADD COLUMN ban_reason VARCHAR(255) NULL");
                log.info("sys_user.ban_reason added");
            }
            log.info("user governance columns checked");
        } catch (Exception ex) {
            log.warn("user governance column init failed: {}", ex.getMessage());
        }
    }

    private boolean hasColumn(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(String.join("\n",
                "SELECT COUNT(1)",
                "FROM information_schema.COLUMNS",
                "WHERE TABLE_SCHEMA = DATABASE()",
                "  AND TABLE_NAME = ?",
                "  AND COLUMN_NAME = ?"
        ), Integer.class, tableName, columnName);
        return count != null && count > 0;
    }

    private void createCommunityPostTable() {
        jdbcTemplate.execute(String.join("\n",
                "CREATE TABLE IF NOT EXISTS community_post (",
                "    id BIGINT PRIMARY KEY AUTO_INCREMENT,",
                "    user_id BIGINT NOT NULL,",
                "    title VARCHAR(120) NOT NULL,",
                "    content TEXT NOT NULL,",
                "    code_snippet MEDIUMTEXT NULL,",
                "    status TINYINT NOT NULL DEFAULT 1,",
                "    best_reply_id BIGINT NULL,",
                "    view_count INT NOT NULL DEFAULT 0,",
                "    last_reply_at DATETIME NULL,",
                "    created_at DATETIME NOT NULL,",
                "    updated_at DATETIME NOT NULL,",
                "    is_deleted TINYINT NOT NULL DEFAULT 0,",
                "    KEY idx_community_post_user (user_id),",
                "    KEY idx_community_post_status (status)",
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        ));
        log.info("community_post checked");
    }

    private void createCommunityReplyTable() {
        jdbcTemplate.execute(String.join("\n",
                "CREATE TABLE IF NOT EXISTS community_reply (",
                "    id BIGINT PRIMARY KEY AUTO_INCREMENT,",
                "    post_id BIGINT NOT NULL,",
                "    user_id BIGINT NOT NULL,",
                "    content TEXT NOT NULL,",
                "    code_snippet MEDIUMTEXT NULL,",
                "    is_best TINYINT NOT NULL DEFAULT 0,",
                "    status TINYINT NOT NULL DEFAULT 1,",
                "    created_at DATETIME NOT NULL,",
                "    updated_at DATETIME NOT NULL,",
                "    is_deleted TINYINT NOT NULL DEFAULT 0,",
                "    KEY idx_community_reply_post (post_id),",
                "    KEY idx_community_reply_user (user_id)",
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        ));
        log.info("community_reply checked");
    }

    private void createCommunityModerationTable() {
        jdbcTemplate.execute(String.join("\n",
                "CREATE TABLE IF NOT EXISTS community_moderation (",
                "    id BIGINT PRIMARY KEY AUTO_INCREMENT,",
                "    target_type VARCHAR(20) NOT NULL,",
                "    target_id BIGINT NOT NULL,",
                "    action VARCHAR(32) NOT NULL,",
                "    reason VARCHAR(255) NULL,",
                "    operator_id BIGINT NOT NULL,",
                "    created_at DATETIME NOT NULL,",
                "    updated_at DATETIME NOT NULL,",
                "    is_deleted TINYINT NOT NULL DEFAULT 0,",
                "    KEY idx_community_moderation_target (target_type, target_id),",
                "    KEY idx_community_moderation_operator (operator_id)",
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        ));
        log.info("community_moderation checked");
    }

    private void createAuditLogTable() {
        jdbcTemplate.execute(String.join("\n",
                "CREATE TABLE IF NOT EXISTS sys_audit_log (",
                "    id BIGINT PRIMARY KEY AUTO_INCREMENT,",
                "    module VARCHAR(64) NOT NULL,",
                "    action VARCHAR(64) NOT NULL,",
                "    target_type VARCHAR(64) NOT NULL,",
                "    target_id BIGINT NULL,",
                "    operator_id BIGINT NULL,",
                "    detail TEXT NULL,",
                "    created_at DATETIME NOT NULL,",
                "    updated_at DATETIME NOT NULL,",
                "    is_deleted TINYINT NOT NULL DEFAULT 0,",
                "    KEY idx_audit_module_action (module, action),",
                "    KEY idx_audit_target (target_type, target_id)",
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        ));
        log.info("sys_audit_log checked");
    }
}