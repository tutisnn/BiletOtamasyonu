package com.example.ucakbiletotamasyonu.controller.impl;

import com.example.ucakbiletotamasyonu.controller.RestBaseController;
import com.example.ucakbiletotamasyonu.controller.RootEntity;
import com.example.ucakbiletotamasyonu.dto.ChatRequest;
import com.example.ucakbiletotamasyonu.dto.ChatResponse;
import com.example.ucakbiletotamasyonu.service.IChatService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
public class RestChatControllerImpl extends RestBaseController {

    private static final Logger log = LoggerFactory.getLogger(RestChatControllerImpl.class);

    private final IChatService chatService;

    public RestChatControllerImpl(IChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public RootEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        log.info("chat endpoint called with message length={}", request.getMessage().length());
        ChatResponse chatResponse = chatService.chat(request.getMessage());
        return ok(chatResponse);
    }
}
