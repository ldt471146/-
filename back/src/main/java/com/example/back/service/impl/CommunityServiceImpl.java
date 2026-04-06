package com.example.back.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.back.dto.AdminCommunityReviewRequest;
import com.example.back.dto.CommunityPostCreateRequest;
import com.example.back.dto.CommunityReplyCreateRequest;
import com.example.back.entity.CommunityModeration;
import com.example.back.entity.CommunityPost;
import com.example.back.entity.CommunityReply;
import com.example.back.entity.SysUser;
import com.example.back.mapper.CommunityModerationMapper;
import com.example.back.mapper.CommunityPostMapper;
import com.example.back.mapper.CommunityReplyMapper;
import com.example.back.mapper.SysUserMapper;
import com.example.back.service.AuditLogService;
import com.example.back.service.CommunityService;
import com.example.back.util.SecurityUtil;
import com.example.back.vo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 社区帖子、回复与治理审核服务
 */
@Service
public class CommunityServiceImpl implements CommunityService {

    private static final int STATUS_NORMAL = 1;
    private static final int STATUS_HIDDEN = 2;

    private final CommunityPostMapper postMapper;
    private final CommunityReplyMapper replyMapper;
    private final CommunityModerationMapper moderationMapper;
    private final SysUserMapper userMapper;
    private final AuditLogService auditLogService;

    public CommunityServiceImpl(CommunityPostMapper postMapper,
                                CommunityReplyMapper replyMapper,
                                CommunityModerationMapper moderationMapper,
                                SysUserMapper userMapper,
                                AuditLogService auditLogService) {
        this.postMapper = postMapper;
        this.replyMapper = replyMapper;
        this.moderationMapper = moderationMapper;
        this.userMapper = userMapper;
        this.auditLogService = auditLogService;
    }

    @Override
    public PageResultVO<CommunityPostVO> listPosts(String keyword, long page, long size) {
        Page<CommunityPost> mpPage = new Page<>(page, size);
        LambdaQueryWrapper<CommunityPost> wrapper = new LambdaQueryWrapper<CommunityPost>()
                .eq(CommunityPost::getStatus, STATUS_NORMAL)
                .orderByDesc(CommunityPost::getLastReplyAt)
                .orderByDesc(CommunityPost::getId);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(CommunityPost::getTitle, keyword.trim())
                    .or()
                    .like(CommunityPost::getContent, keyword.trim()));
        }
        Page<CommunityPost> result = postMapper.selectPage(mpPage, wrapper);
        return toPostPageResult(result, page, size);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommunityPostDetailVO postDetail(Long postId) {
        CommunityPost post = postMapper.selectById(postId);
        if (post == null || post.getStatus() == null || post.getStatus() != STATUS_NORMAL) {
            throw new IllegalArgumentException("帖子不存在或暂不可回复");
        }

        post.setViewCount((post.getViewCount() == null ? 0 : post.getViewCount()) + 1);
        postMapper.updateById(post);

        List<CommunityReply> replies = replyMapper.selectList(new LambdaQueryWrapper<CommunityReply>()
                .eq(CommunityReply::getPostId, postId)
                .eq(CommunityReply::getStatus, STATUS_NORMAL)
                .orderByDesc(CommunityReply::getIsBest)
                .orderByAsc(CommunityReply::getId));

        Set<Long> userIds = replies.stream().map(CommunityReply::getUserId).collect(Collectors.toSet());
        userIds.add(post.getUserId());
        Map<Long, String> nameMap = userNameMap(userIds);

        CommunityPostDetailVO vo = new CommunityPostDetailVO();
        vo.setId(post.getId());
        vo.setAuthorId(post.getUserId());
        vo.setAuthorName(nameMap.getOrDefault(post.getUserId(), "用户" + post.getUserId()));
        vo.setTitle(post.getTitle());
        vo.setContent(post.getContent());
        vo.setCodeSnippet(post.getCodeSnippet());
        vo.setStatus(post.getStatus());
        vo.setBestReplyId(post.getBestReplyId());
        vo.setViewCount(post.getViewCount());
        vo.setCreatedAt(post.getCreatedAt());
        vo.setLastReplyAt(post.getLastReplyAt());
        vo.setReplies(replies.stream().map(reply -> toReplyVO(reply, nameMap)).collect(Collectors.toList()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPost(CommunityPostCreateRequest request) {
        SysUser user = requireInteractiveUser();

        CommunityPost post = new CommunityPost();
        post.setUserId(user.getId());
        post.setTitle(request.getTitle().trim());
        post.setContent(request.getContent().trim());
        post.setCodeSnippet(trimToNull(request.getCodeSnippet()));
        post.setStatus(STATUS_NORMAL);
        post.setViewCount(0);
        post.setLastReplyAt(LocalDateTime.now());
        postMapper.insert(post);
        return post.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createReply(Long postId, CommunityReplyCreateRequest request) {
        SysUser user = requireInteractiveUser();

        CommunityPost post = postMapper.selectById(postId);
        if (post == null || post.getStatus() == null || post.getStatus() != STATUS_NORMAL) {
            throw new IllegalArgumentException("帖子不存在或暂不可回复");
        }

        CommunityReply reply = new CommunityReply();
        reply.setPostId(postId);
        reply.setUserId(user.getId());
        reply.setContent(request.getContent().trim());
        reply.setCodeSnippet(trimToNull(request.getCodeSnippet()));
        reply.setIsBest(0);
        reply.setStatus(STATUS_NORMAL);
        replyMapper.insert(reply);

        post.setLastReplyAt(LocalDateTime.now());
        postMapper.updateById(post);
        return reply.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markBestAnswer(Long postId, Long replyId) {
        CommunityPost post = postMapper.selectById(postId);
        if (post == null || post.getStatus() == null || post.getStatus() != STATUS_NORMAL) {
            throw new IllegalArgumentException("帖子不存在");
        }
        CommunityReply target = replyMapper.selectById(replyId);
        if (target == null || !postId.equals(target.getPostId())
                || target.getStatus() == null || target.getStatus() != STATUS_NORMAL) {
            throw new IllegalArgumentException("回复不存在");
        }

        List<CommunityReply> bestList = replyMapper.selectList(new LambdaQueryWrapper<CommunityReply>()
                .eq(CommunityReply::getPostId, postId)
                .eq(CommunityReply::getIsBest, 1));
        for (CommunityReply old : bestList) {
            old.setIsBest(0);
            replyMapper.updateById(old);
        }

        target.setIsBest(1);
        replyMapper.updateById(target);
        post.setBestReplyId(replyId);
        postMapper.updateById(post);
    }

    @Override
    public PageResultVO<CommunityPostVO> adminListPosts(String keyword, Integer status, long page, long size) {
        Page<CommunityPost> mpPage = new Page<>(page, size);
        LambdaQueryWrapper<CommunityPost> wrapper = new LambdaQueryWrapper<CommunityPost>()
                .eq(status != null, CommunityPost::getStatus, status)
                .orderByDesc(CommunityPost::getId);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(CommunityPost::getTitle, keyword.trim())
                    .or()
                    .like(CommunityPost::getContent, keyword.trim()));
        }
        Page<CommunityPost> result = postMapper.selectPage(mpPage, wrapper);
        return toPostPageResult(result, page, size);
    }

    @Override
    public PageResultVO<CommunityReplyVO> adminListReplies(Long postId, Integer status, long page, long size) {
        Page<CommunityReply> mpPage = new Page<>(page, size);
        LambdaQueryWrapper<CommunityReply> wrapper = new LambdaQueryWrapper<CommunityReply>()
                .eq(postId != null, CommunityReply::getPostId, postId)
                .eq(status != null, CommunityReply::getStatus, status)
                .orderByDesc(CommunityReply::getId);
        Page<CommunityReply> result = replyMapper.selectPage(mpPage, wrapper);

        Set<Long> userIds = result.getRecords().stream()
                .map(CommunityReply::getUserId)
                .collect(Collectors.toSet());
        Map<Long, String> nameMap = userNameMap(userIds);

        List<CommunityReplyVO> records = result.getRecords().stream()
                .map(v -> toReplyVO(v, nameMap))
                .collect(Collectors.toList());
        PageResultVO<CommunityReplyVO> vo = new PageResultVO<>();
        vo.setPage(page);
        vo.setSize(size);
        vo.setTotal(result.getTotal());
        vo.setRecords(records);
        return vo;
    }

    @Override
    public CommunityModerationOverviewVO adminOverview() {
        CommunityModerationOverviewVO vo = new CommunityModerationOverviewVO();
        vo.setTotalPosts(postMapper.selectCount(new LambdaQueryWrapper<CommunityPost>()));
        vo.setNormalPosts(postMapper.selectCount(new LambdaQueryWrapper<CommunityPost>().eq(CommunityPost::getStatus, STATUS_NORMAL)));
        vo.setHiddenPosts(postMapper.selectCount(new LambdaQueryWrapper<CommunityPost>().eq(CommunityPost::getStatus, STATUS_HIDDEN)));
        vo.setTotalReplies(replyMapper.selectCount(new LambdaQueryWrapper<CommunityReply>()));
        vo.setNormalReplies(replyMapper.selectCount(new LambdaQueryWrapper<CommunityReply>().eq(CommunityReply::getStatus, STATUS_NORMAL)));
        vo.setHiddenReplies(replyMapper.selectCount(new LambdaQueryWrapper<CommunityReply>().eq(CommunityReply::getStatus, STATUS_HIDDEN)));
        vo.setBestReplyCount(replyMapper.selectCount(new LambdaQueryWrapper<CommunityReply>().eq(CommunityReply::getIsBest, 1)));
        vo.setMutedUsers(userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getMuteStatus, 1)));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewPost(Long postId, AdminCommunityReviewRequest request) {
        CommunityPost post = postMapper.selectById(postId);
        if (post == null) {
            throw new IllegalArgumentException("帖子不存在");
        }
        String action = normalizeAction(request.getAction());
        String reason = trimToNull(request.getReason());

        if ("APPROVE".equals(action)) {
            post.setStatus(STATUS_NORMAL);
        } else if ("DELETE".equals(action)) {
            post.setStatus(STATUS_HIDDEN);
        } else if ("MUTE_USER".equals(action)) {
            post.setStatus(STATUS_HIDDEN);
            muteUser(post.getUserId(), reason);
        } else {
            throw new IllegalArgumentException("审核动作不合法");
        }
        postMapper.updateById(post);
        saveModeration("POST", postId, action, reason);
        auditLogService.log("COMMUNITY", action, "POST", postId, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewReply(Long replyId, AdminCommunityReviewRequest request) {
        CommunityReply reply = replyMapper.selectById(replyId);
        if (reply == null) {
            throw new IllegalArgumentException("回复不存在");
        }
        String action = normalizeAction(request.getAction());
        String reason = trimToNull(request.getReason());

        if ("APPROVE".equals(action)) {
            reply.setStatus(STATUS_NORMAL);
        } else if ("DELETE".equals(action)) {
            reply.setStatus(STATUS_HIDDEN);
        } else if ("MUTE_USER".equals(action)) {
            reply.setStatus(STATUS_HIDDEN);
            muteUser(reply.getUserId(), reason);
        } else {
            throw new IllegalArgumentException("审核动作不合法");
        }
        replyMapper.updateById(reply);
        saveModeration("REPLY", replyId, action, reason);
        auditLogService.log("COMMUNITY", action, "REPLY", replyId, reason);
    }

    private PageResultVO<CommunityPostVO> toPostPageResult(Page<CommunityPost> result, long page, long size) {
        Set<Long> userIds = result.getRecords().stream()
                .map(CommunityPost::getUserId)
                .collect(Collectors.toSet());
        Map<Long, String> nameMap = userNameMap(userIds);
        List<CommunityPostVO> records = result.getRecords().stream().map(post -> {
            CommunityPostVO vo = new CommunityPostVO();
            vo.setId(post.getId());
            vo.setAuthorId(post.getUserId());
            vo.setAuthorName(nameMap.getOrDefault(post.getUserId(), "用户" + post.getUserId()));
            vo.setTitle(post.getTitle());
            vo.setContentPreview(toPreview(post.getContent()));
            vo.setStatus(post.getStatus());
            vo.setBestReplyId(post.getBestReplyId());
            vo.setReplyCount(replyMapper.selectCount(new LambdaQueryWrapper<CommunityReply>()
                    .eq(CommunityReply::getPostId, post.getId())
                    .eq(CommunityReply::getStatus, STATUS_NORMAL)).intValue());
            vo.setViewCount(post.getViewCount() == null ? 0 : post.getViewCount());
            vo.setCreatedAt(post.getCreatedAt());
            vo.setLastReplyAt(post.getLastReplyAt());
            return vo;
        }).collect(Collectors.toList());

        PageResultVO<CommunityPostVO> vo = new PageResultVO<>();
        vo.setPage(page);
        vo.setSize(size);
        vo.setTotal(result.getTotal());
        vo.setRecords(records);
        return vo;
    }

    private CommunityReplyVO toReplyVO(CommunityReply reply, Map<Long, String> nameMap) {
        CommunityReplyVO vo = new CommunityReplyVO();
        vo.setId(reply.getId());
        vo.setPostId(reply.getPostId());
        vo.setAuthorId(reply.getUserId());
        vo.setAuthorName(nameMap.getOrDefault(reply.getUserId(), "用户" + reply.getUserId()));
        vo.setContent(reply.getContent());
        vo.setCodeSnippet(reply.getCodeSnippet());
        vo.setIsBest(reply.getIsBest());
        vo.setStatus(reply.getStatus());
        vo.setCreatedAt(reply.getCreatedAt());
        return vo;
    }

    private SysUser requireInteractiveUser() {
        Long userId = SecurityUtil.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("用户未登录");
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            throw new IllegalArgumentException("账号不可用");
        }
        if (user.getMuteStatus() != null && user.getMuteStatus() == 1) {
            throw new IllegalArgumentException("当前账号已被禁言");
        }
        return user;
    }

    private void muteUser(Long userId, String reason) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }
        user.setMuteStatus(1);
        if (reason != null && !reason.isBlank()) {
            user.setBanReason(reason);
        }
        userMapper.updateById(user);
    }

    private void saveModeration(String targetType, Long targetId, String action, String reason) {
        CommunityModeration moderation = new CommunityModeration();
        moderation.setTargetType(targetType);
        moderation.setTargetId(targetId);
        moderation.setAction(action);
        moderation.setReason(reason);
        moderation.setOperatorId(SecurityUtil.getUserId());
        moderationMapper.insert(moderation);
    }

    private Map<Long, String> userNameMap(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, SysUser::getUsername));
    }

    private String toPreview(String content) {
        if (content == null) {
            return "";
        }
        String c = content.trim();
        if (c.length() <= 80) {
            return c;
        }
        return c.substring(0, 80) + "...";
    }

    private String normalizeAction(String action) {
        if (action == null) {
            return "";
        }
        return action.trim().toUpperCase();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
