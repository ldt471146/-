package com.example.back.service;

import com.example.back.vo.ReportOverviewVO;

/**
 * 成长报告服务
 */
public interface ReportService {
    ReportOverviewVO overview();

    com.example.back.vo.ReportTrendVO trend();
}
