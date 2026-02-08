package com.example.back.service;

import com.example.back.dto.AiChatRequest;
import com.example.back.vo.AiChatResponseVO;

/**
 * AI 助手服务
 */
public interface AiService {
    AiChatResponseVO chat(AiChatRequest request);
}
