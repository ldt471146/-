package com.example.back.controller;

import com.example.back.common.ApiResponse;
import com.example.back.dto.CommunityPostCreateRequest;
import com.example.back.dto.CommunityReplyCreateRequest;
import com.example.back.service.CommunityService;
import com.example.back.vo.CommunityPostDetailVO;
import com.example.back.vo.CommunityPostVO;
import com.example.back.vo.PageResultVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 社区接口（学生/教师/管理员）
 */
@RestController
@RequestMapping("/api/community")
@PreAuthorize("hasAnyRole('STUDENT','TEACHER','ADMIN')")
public class CommunityController {

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @GetMapping("/posts")
    public ApiResponse<PageResultVO<CommunityPostVO>> posts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "1") long page,
            @RequestParam(value = "size", defaultValue = "10") long size
    ) {
        return ApiResponse.ok(communityService.listPosts(keyword, page, size));
    }

    @GetMapping("/posts/{id}")
    public ApiResponse<CommunityPostDetailVO> detail(@PathVariable("id") Long id) {
        return ApiResponse.ok(communityService.postDetail(id));
    }

    @PostMapping("/posts")
    public ApiResponse<Long> createPost(@Valid @RequestBody CommunityPostCreateRequest request) {
        return ApiResponse.ok(communityService.createPost(request));
    }

    @PostMapping("/posts/{id}/replies")
    public ApiResponse<Long> createReply(@PathVariable("id") Long id,
                                         @Valid @RequestBody CommunityReplyCreateRequest request) {
        return ApiResponse.ok(communityService.createReply(id, request));
    }

    @PostMapping("/posts/{postId}/best/{replyId}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<Void> markBest(@PathVariable("postId") Long postId,
                                      @PathVariable("replyId") Long replyId) {
        communityService.markBestAnswer(postId, replyId);
        return ApiResponse.ok();
    }
}
