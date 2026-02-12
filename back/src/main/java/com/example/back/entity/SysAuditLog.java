package com.example.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 审计日志
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_audit_log")
public class SysAuditLog extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String module;

    private String action;

    private String targetType;

    private Long targetId;

    private Long operatorId;

    private String detail;
}
