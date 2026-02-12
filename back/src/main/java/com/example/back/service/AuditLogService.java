package com.example.back.service;

public interface AuditLogService {

    void log(String module, String action, String targetType, Long targetId, String detail);
}
