package com.example.ucakbiletotamasyonu.service.impl;

import com.example.ucakbiletotamasyonu.service.IChatService;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

@Service
public class ChatServiceImpl implements IChatService {

    private final OpenAiChatModel chatModel;

    public ChatServiceImpl(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public String chat(String message) {
        return this.chatModel.call(message);
    }
}
