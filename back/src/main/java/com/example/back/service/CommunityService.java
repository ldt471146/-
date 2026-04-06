package com.example.back.service;

import com.example.back.dto.AdminCommunityReviewRequest;
import com.example.back.dto.CommunityPostCreateRequest;
import com.example.back.dto.CommunityReplyCreateRequest;
import com.example.back.vo.CommunityModerationOverviewVO;
import com.example.back.vo.CommunityPostDetailVO;
import com.example.back.vo.CommunityPostVO;
import com.example.back.vo.CommunityReplyVO;
import com.example.back.vo.PageResultVO;

public interface CommunityService {

    PageResultVO<CommunityPostVO> listPosts(String keyword, long page, long size);

    CommunityPostDetailVO postDetail(Long postId);

    Long createPost(CommunityPostCreateRequest request);

    Long createReply(Long postId, CommunityReplyCreateRequest request);

    void markBestAnswer(Long postId, Long replyId);

    PageResultVO<CommunityPostVO> adminListPosts(String keyword, Integer status, long page, long size);

    PageResultVO<CommunityReplyVO> adminListReplies(Long postId, Integer status, long page, long size);

    CommunityModerationOverviewVO adminOverview();

    void reviewPost(Long postId, AdminCommunityReviewRequest request);

    void reviewReply(Long replyId, AdminCommunityReviewRequest request);
}