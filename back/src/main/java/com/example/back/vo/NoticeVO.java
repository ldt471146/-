package com.example.back.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知返回
 */
@Data
public class NoticeVO {
    private Long id;
    private String title;
    private String content;
    private String type;
    private LocalDateTime createdAt;
    private Integer isRead;
}
