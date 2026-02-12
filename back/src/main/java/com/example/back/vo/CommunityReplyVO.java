package com.example.back.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 社区回复
 */
@Data
public class CommunityReplyVO {
    private Long id;
    private Long postId;
    private Long authorId;
    private String authorName;
    private String content;
    private String codeSnippet;
    private Integer isBest;
    private Integer status;
    private LocalDateTime createdAt;
}
