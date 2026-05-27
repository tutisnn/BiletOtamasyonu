package com.example.ucakbiletotamasyonu.service;

import com.example.ucakbiletotamasyonu.dto.VoiceAssistantResponse;
import org.springframework.core.io.Resource;

public interface IVoiceAssistantService {

    VoiceAssistantResponse processAudio(Resource audioResource);

    byte[] getAudio(String conversationId);
}
