package com.example.back.service;

import com.example.back.vo.NoticeVO;
import com.example.back.vo.PageResultVO;

public interface NoticeService {
    PageResultVO<NoticeVO> list(long page, long size);

    void markRead(Long noticeId);

    void markAllRead();

    Long unreadCount();

    void deleteNotice(Long noticeId);
}
