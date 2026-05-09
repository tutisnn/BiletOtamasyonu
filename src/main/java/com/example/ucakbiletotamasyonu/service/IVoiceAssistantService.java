package com.example.ucakbiletotamasyonu.service;

import com.example.ucakbiletotamasyonu.dto.VoiceAssistantResponse;
import org.springframework.core.io.Resource;

public interface IVoiceAssistantService {

    VoiceAssistantResponse processAudio(Resource audioResource, String conversationId);

    byte[] getAudio(String conversationId);

    void clearConversation(String conversationId);
}
