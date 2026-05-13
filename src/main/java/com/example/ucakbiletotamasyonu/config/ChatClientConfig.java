package com.example.ucakbiletotamasyonu.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                        You are a helpful flight booking assistant.
                        Keep answers concise, natural, and in Turkish unless the user explicitly asks otherwise.
                        """)
                .build();
    }
}
