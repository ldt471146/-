package com.example.back.controller;

import com.example.back.common.ApiResponse;
import com.example.back.dto.AiChatRequest;
import com.example.back.service.AiService;
import com.example.back.vo.AiChatResponseVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 助手接口
 */
@RestController
@RequestMapping("/api/assistant")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/chat")
    public ApiResponse<AiChatResponseVO> chat(@RequestBody AiChatRequest request) {
        return ApiResponse.ok(aiService.chat(request));
    }
}
