package com.example.ucakbiletotamasyonu.controller;

import com.example.ucakbiletotamasyonu.dto.VoiceAssistantResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

public interface IRestVoiceAssistantController {

    RootEntity<VoiceAssistantResponse> processAudio(MultipartFile audio, String conversationId, HttpSession session);

    ResponseEntity<byte[]> getAudio(String conversationId);

    RootEntity<String> clearConversation(String conversationId, HttpSession session);
}
