package com.example.back.controller;

import com.example.back.common.ApiResponse;
import com.example.back.service.NoticeService;
import com.example.back.vo.NoticeVO;
import com.example.back.vo.PageResultVO;
import org.springframework.web.bind.annotation.*;

/**
 * 通知中心
 */
@RestController
@RequestMapping("/api/notices")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping
    public ApiResponse<PageResultVO<NoticeVO>> list(
            @RequestParam(value = "page", defaultValue = "1") long page,
            @RequestParam(value = "size", defaultValue = "10") long size
    ) {
        return ApiResponse.ok(noticeService.list(page, size));
    }

    @PostMapping("/{id}/read")
    public ApiResponse<Void> read(@PathVariable("id") Long id) {
        noticeService.markRead(id);
        return ApiResponse.ok();
    }

    @PostMapping("/read-all")
    public ApiResponse<Void> readAll() {
        noticeService.markAllRead();
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        noticeService.deleteNotice(id);
        return ApiResponse.ok();
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> unreadCount() {
        return ApiResponse.ok(noticeService.unreadCount());
    }
}
