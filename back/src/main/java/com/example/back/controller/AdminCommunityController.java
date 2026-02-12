package com.example.back.controller;

import com.example.back.common.ApiResponse;
import com.example.back.dto.AdminCommunityReviewRequest;
import com.example.back.service.CommunityService;
import com.example.back.vo.CommunityPostVO;
import com.example.back.vo.CommunityReplyVO;
import com.example.back.vo.PageResultVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员-社区审核
 */
@RestController
@RequestMapping("/api/admin/community")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCommunityController {

    private final CommunityService communityService;

    public AdminCommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @GetMapping("/posts")
    public ApiResponse<PageResultVO<CommunityPostVO>> posts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "page", defaultValue = "1") long page,
            @RequestParam(value = "size", defaultValue = "10") long size
    ) {
        return ApiResponse.ok(communityService.adminListPosts(keyword, status, page, size));
    }

    @GetMapping("/replies")
    public ApiResponse<PageResultVO<CommunityReplyVO>> replies(
            @RequestParam(value = "postId", required = false) Long postId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "page", defaultValue = "1") long page,
            @RequestParam(value = "size", defaultValue = "10") long size
    ) {
        return ApiResponse.ok(communityService.adminListReplies(postId, status, page, size));
    }

    @PostMapping("/posts/{id}/review")
    public ApiResponse<Void> reviewPost(@PathVariable("id") Long id,
                                        @Valid @RequestBody AdminCommunityReviewRequest request) {
        communityService.reviewPost(id, request);
        return ApiResponse.ok();
    }

    @PostMapping("/replies/{id}/review")
    public ApiResponse<Void> reviewReply(@PathVariable("id") Long id,
                                         @Valid @RequestBody AdminCommunityReviewRequest request) {
        communityService.reviewReply(id, request);
        return ApiResponse.ok();
    }
}
