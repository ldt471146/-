package com.example.back.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 社区与审计表初始化
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
                log.info("已新增字段: sys_user.mute_status");
            }
            if (!hasColumn("sys_user", "ban_reason")) {
                jdbcTemplate.execute("ALTER TABLE sys_user ADD COLUMN ban_reason VARCHAR(255) NULL");
                log.info("已新增字段: sys_user.ban_reason");
            }
            log.info("用户治理字段检查完成");
        } catch (Exception e) {
            log.warn("用户治理字段初始化失败: {}", e.getMessage());
        }
    }

    private boolean hasColumn(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(1)
                        FROM information_schema.COLUMNS
                        WHERE TABLE_SCHEMA = DATABASE()
                          AND TABLE_NAME = ?
                          AND COLUMN_NAME = ?
                        """,
                Integer.class,
                tableName,
                columnName
        );
        return count != null && count > 0;
    }

    private void createCommunityPostTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS community_post (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    user_id BIGINT NOT NULL,
                    title VARCHAR(120) NOT NULL,
                    content TEXT NOT NULL,
                    code_snippet MEDIUMTEXT NULL,
                    status TINYINT NOT NULL DEFAULT 1,
                    best_reply_id BIGINT NULL,
                    view_count INT NOT NULL DEFAULT 0,
                    last_reply_at DATETIME NULL,
                    created_at DATETIME NOT NULL,
                    updated_at DATETIME NOT NULL,
                    is_deleted TINYINT NOT NULL DEFAULT 0,
                    KEY idx_community_post_user (user_id),
                    KEY idx_community_post_status (status)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
        log.info("社区帖子表检查完成");
    }

    private void createCommunityReplyTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS community_reply (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    post_id BIGINT NOT NULL,
                    user_id BIGINT NOT NULL,
                    content TEXT NOT NULL,
                    code_snippet MEDIUMTEXT NULL,
                    is_best TINYINT NOT NULL DEFAULT 0,
                    status TINYINT NOT NULL DEFAULT 1,
                    created_at DATETIME NOT NULL,
                    updated_at DATETIME NOT NULL,
                    is_deleted TINYINT NOT NULL DEFAULT 0,
                    KEY idx_community_reply_post (post_id),
                    KEY idx_community_reply_user (user_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
        log.info("社区回复表检查完成");
    }

    private void createCommunityModerationTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS community_moderation (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    target_type VARCHAR(20) NOT NULL,
                    target_id BIGINT NOT NULL,
                    action VARCHAR(32) NOT NULL,
                    reason VARCHAR(255) NULL,
                    operator_id BIGINT NOT NULL,
                    created_at DATETIME NOT NULL,
                    updated_at DATETIME NOT NULL,
                    is_deleted TINYINT NOT NULL DEFAULT 0,
                    KEY idx_community_moderation_target (target_type, target_id),
                    KEY idx_community_moderation_operator (operator_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
        log.info("社区审核记录表检查完成");
    }

    private void createAuditLogTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS sys_audit_log (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    module VARCHAR(64) NOT NULL,
                    action VARCHAR(64) NOT NULL,
                    target_type VARCHAR(64) NOT NULL,
                    target_id BIGINT NULL,
                    operator_id BIGINT NULL,
                    detail TEXT NULL,
                    created_at DATETIME NOT NULL,
                    updated_at DATETIME NOT NULL,
                    is_deleted TINYINT NOT NULL DEFAULT 0,
                    KEY idx_audit_module_action (module, action),
                    KEY idx_audit_target (target_type, target_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
        log.info("审计日志表检查完成");
    }
}
