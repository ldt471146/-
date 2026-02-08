package com.example.back.vo;

import lombok.Data;

/**
 * 课时返回
 */
@Data
public class LessonVO {
    private Long id;
    private String title;
    private String contentType;
    private String contentUrl;
    private String contentText;
    private Integer sortNo;
}
