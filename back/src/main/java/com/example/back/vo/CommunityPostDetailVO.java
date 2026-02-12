package com.example.back.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 社区帖子详情
 */
@Data
public class CommunityPostDetailVO {
    private Long id;
    private Long authorId;
    private String authorName;
    private String title;
    private String content;
    private String codeSnippet;
    private Integer status;
    private Long bestReplyId;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime lastReplyAt;
    private List<CommunityReplyVO> replies;
}
