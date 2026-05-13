package com.example.ucakbiletotamasyonu.service.impl;

import com.example.ucakbiletotamasyonu.dto.ChatResponse;
import com.example.ucakbiletotamasyonu.dto.VoiceAssistantResponse;
import com.example.ucakbiletotamasyonu.exception.BaseException;
import com.example.ucakbiletotamasyonu.exception.ErrorMessage;
import com.example.ucakbiletotamasyonu.exception.MessageType;
import com.example.ucakbiletotamasyonu.service.IChatService;
import com.example.ucakbiletotamasyonu.service.IVoiceAssistantService;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class VoiceAssistantServiceImpl implements IVoiceAssistantService {

    private static final Logger log = LoggerFactory.getLogger(VoiceAssistantServiceImpl.class);
    private static final String AUDIO_CONTENT_TYPE = "audio/mpeg";

    private final TranscriptionModel transcriptionModel;
    private final TextToSpeechModel textToSpeechModel;
    private final IChatService chatService;
    private final ChatMemory chatMemory;
    private final ConcurrentHashMap<String, byte[]> audioCache = new ConcurrentHashMap<>();

    public VoiceAssistantServiceImpl(ChatMemory chatMemory,
                                     TranscriptionModel transcriptionModel,
                                     TextToSpeechModel textToSpeechModel,
                                     IChatService chatService) {
        this.chatMemory = chatMemory;
        this.transcriptionModel = transcriptionModel;
        this.textToSpeechModel = textToSpeechModel;
        this.chatService = chatService;
    }

    @Override
    public VoiceAssistantResponse processAudio(Resource audioResource, String conversationId) {
        String normalizedConversationId = normalizeConversationId(conversationId);
        try {
            log.info("voice assistant request started, conversationId={}, audioResource={}",
                    normalizedConversationId,
                    audioResource.getDescription());

            String transcript = transcriptionModel.transcribe(audioResource);
            log.info("voice assistant transcription completed, conversationId={}, transcriptLength={}",
                    normalizedConversationId,
                    transcript == null ? 0 : transcript.length());

            ChatResponse chatResponse = chatService.chat(transcript == null ? "" : transcript);
            String answer = chatResponse == null ? null : chatResponse.getAssistantText();

            log.info("voice assistant chat completed, conversationId={}, answerLength={}",
                    normalizedConversationId,
                    answer == null ? 0 : answer.length());

            byte[] audioBytes = textToSpeechModel.call(answer == null ? "" : answer);
            log.info("voice assistant tts completed, conversationId={}, audioBytes={}",
                    normalizedConversationId,
                    audioBytes == null ? 0 : audioBytes.length);

            audioCache.put(normalizedConversationId, audioBytes);

            return new VoiceAssistantResponse(
                    normalizedConversationId,
                    transcript,
                    chatResponse == null ? null : chatResponse.getType(),
                    answer,
                    chatResponse == null ? null : chatResponse.getData(),
                    "/api/v1/voice/audio/" + normalizedConversationId,
                    AUDIO_CONTENT_TYPE
            );
        } catch (Exception e) {
            log.warn("voice assistant processing failed, conversationId={}, message={}",
                    normalizedConversationId,
                    e.getMessage());
            throw new BaseException(new ErrorMessage(MessageType.VOICE_ASSISTANT_FAILED, e.getMessage()));
        }
    }

    @Override
    public void clearConversation(String conversationId) {
        String normalizedConversationId = normalizeConversationId(conversationId);
        chatMemory.clear(normalizedConversationId);
        audioCache.remove(normalizedConversationId);
        log.info("voice assistant conversation cleared, conversationId={}", normalizedConversationId);
    }

    @Override
    public byte[] getAudio(String conversationId) {
        String normalizedConversationId = normalizeConversationId(conversationId);
        byte[] audio = audioCache.get(normalizedConversationId);
        if (audio == null || audio.length == 0) {
            throw new BaseException(new ErrorMessage(MessageType.VOICE_AUDIO_NOT_FOUND, normalizedConversationId));
        }
        return audio;
    }

    private String normalizeConversationId(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return conversationId.trim();
    }
}