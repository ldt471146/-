package com.example.back.controller;

import com.example.back.common.ApiResponse;
import com.example.back.service.ReportService;
import com.example.back.vo.ReportOverviewVO;
import com.example.back.vo.ReportTrendVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 成长报告接口
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/overview")
    public ApiResponse<ReportOverviewVO> overview() {
        return ApiResponse.ok(reportService.overview());
    }

    @GetMapping("/trend")
    public ApiResponse<ReportTrendVO> trend() {
        return ApiResponse.ok(reportService.trend());
    }
}
