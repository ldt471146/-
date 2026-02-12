package com.example.back.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新建社区回复
 */
@Data
public class CommunityReplyCreateRequest {

    @NotBlank(message = "回复内容不能为空")
    @Size(max = 3000, message = "回复内容长度不能超过3000")
    private String content;

    @Size(max = 8000, message = "代码片段长度不能超过8000")
    private String codeSnippet;
}
