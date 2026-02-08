package com.example.back.dto;

import lombok.Data;

import java.util.List;

/**
 * AI 对话请求
 */
@Data
public class AiChatRequest {
    private String message;
    private List<AiMessage> history;

    @Data
    public static class AiMessage {
        private String role;
        private String content;
    }
}
