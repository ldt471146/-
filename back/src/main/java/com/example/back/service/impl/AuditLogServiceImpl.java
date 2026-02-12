package com.example.back.service.impl;

import com.example.back.entity.SysAuditLog;
import com.example.back.mapper.SysAuditLogMapper;
import com.example.back.service.AuditLogService;
import com.example.back.util.SecurityUtil;
import org.springframework.stereotype.Service;

/**
 * 审计日志服务
 */
@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final SysAuditLogMapper auditLogMapper;

    public AuditLogServiceImpl(SysAuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    @Override
    public void log(String module, String action, String targetType, Long targetId, String detail) {
        SysAuditLog log = new SysAuditLog();
        log.setModule(module);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setOperatorId(SecurityUtil.getUserId());
        log.setDetail(detail);
        auditLogMapper.insert(log);
    }
}
