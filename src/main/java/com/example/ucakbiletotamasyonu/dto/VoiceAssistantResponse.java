package com.example.ucakbiletotamasyonu.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VoiceAssistantResponse {

    private String conversationId;
    private String transcript;
    private String answer;
    private String audioUrl;
    private String audioContentType;
}
