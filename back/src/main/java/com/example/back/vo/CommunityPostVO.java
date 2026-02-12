package com.example.back.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 社区帖子列表
 */
@Data
public class CommunityPostVO {
    private Long id;
    private Long authorId;
    private String authorName;
    private String title;
    private String contentPreview;
    private Integer status;
    private Long bestReplyId;
    private Integer replyCount;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime lastReplyAt;
}
