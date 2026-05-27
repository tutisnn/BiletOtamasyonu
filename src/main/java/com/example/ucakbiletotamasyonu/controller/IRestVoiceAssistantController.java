package com.example.ucakbiletotamasyonu.controller;

import com.example.ucakbiletotamasyonu.dto.VoiceAssistantResponse;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

public interface IRestVoiceAssistantController {

    RootEntity<VoiceAssistantResponse> processAudio(MultipartFile audio);

    ResponseEntity<byte[]> getAudio(String conversationId);
}
