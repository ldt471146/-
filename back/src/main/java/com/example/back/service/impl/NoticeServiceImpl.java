package com.example.back.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.back.entity.SysNoticeUser;
import com.example.back.mapper.SysNoticeMapper;
import com.example.back.mapper.SysNoticeUserMapper;
import com.example.back.service.NoticeService;
import com.example.back.util.SecurityUtil;
import com.example.back.vo.NoticeVO;
import com.example.back.vo.PageResultVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NoticeServiceImpl implements NoticeService {

    private final SysNoticeMapper noticeMapper;
    private final SysNoticeUserMapper noticeUserMapper;

    public NoticeServiceImpl(SysNoticeMapper noticeMapper, SysNoticeUserMapper noticeUserMapper) {
        this.noticeMapper = noticeMapper;
        this.noticeUserMapper = noticeUserMapper;
    }

    @Override
    public PageResultVO<NoticeVO> list(long page, long size) {
        Long userId = SecurityUtil.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        long offset = (page - 1) * size;
        List<NoticeVO> records = noticeMapper.list(userId, offset, size);
        long total = noticeMapper.countAll(userId);

        PageResultVO<NoticeVO> vo = new PageResultVO<>();
        vo.setPage(page);
        vo.setSize(size);
        vo.setTotal(total);
        vo.setRecords(records);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long noticeId) {
        Long userId = SecurityUtil.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        SysNoticeUser existing = noticeUserMapper.selectOne(new LambdaQueryWrapper<SysNoticeUser>()
                .eq(SysNoticeUser::getUserId, userId)
                .eq(SysNoticeUser::getNoticeId, noticeId));
        if (existing != null) {
            existing.setIsRead(1);
            noticeUserMapper.updateById(existing);
            return;
        }
        SysNoticeUser nu = new SysNoticeUser();
        nu.setUserId(userId);
        nu.setNoticeId(noticeId);
        nu.setIsRead(1);
        noticeUserMapper.insert(nu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllRead() {
        Long userId = SecurityUtil.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        noticeUserMapper.markAllRead(userId);
        noticeUserMapper.insertAllReadIfMissing(userId);
    }

    @Override
    public Long unreadCount() {
        Long userId = SecurityUtil.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        return noticeMapper.countUnread(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNotice(Long noticeId) {
        Long userId = SecurityUtil.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        int affected = noticeUserMapper.deleteByUser(userId, noticeId);
        if (affected > 0) {
            return;
        }
        SysNoticeUser nu = new SysNoticeUser();
        nu.setUserId(userId);
        nu.setNoticeId(noticeId);
        nu.setIsRead(1);
        nu.setIsDeleted(1);
        noticeUserMapper.insert(nu);
    }
}
