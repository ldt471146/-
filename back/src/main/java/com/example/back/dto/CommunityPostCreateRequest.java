package com.example.back.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新建社区帖子
 */
@Data
public class CommunityPostCreateRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 120, message = "标题长度不能超过120")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Size(max = 5000, message = "内容长度不能超过5000")
    private String content;

    @Size(max = 8000, message = "代码片段长度不能超过8000")
    private String codeSnippet;
}
