package com.example.ucakbiletotamasyonu.controller.impl;

import com.example.ucakbiletotamasyonu.controller.IRestVoiceAssistantController;
import com.example.ucakbiletotamasyonu.controller.RestBaseController;
import com.example.ucakbiletotamasyonu.controller.RootEntity;
import com.example.ucakbiletotamasyonu.dto.VoiceAssistantResponse;
import com.example.ucakbiletotamasyonu.exception.BaseException;
import com.example.ucakbiletotamasyonu.exception.ErrorMessage;
import com.example.ucakbiletotamasyonu.exception.MessageType;
import com.example.ucakbiletotamasyonu.service.IVoiceAssistantService;
import java.io.IOException;
import java.util.UUID;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/voice")
@Tag(name = "Voice Assistant", description = "Sesli asistan ses yükleme, transcribe etme ve cevap üretme endpointleri")
public class RestVoiceAssistantControllerImpl extends RestBaseController implements IRestVoiceAssistantController {

    private static final Logger log = LoggerFactory.getLogger(RestVoiceAssistantControllerImpl.class);

    private final IVoiceAssistantService voiceAssistantService;

    public RestVoiceAssistantControllerImpl(IVoiceAssistantService voiceAssistantService) {
        this.voiceAssistantService = voiceAssistantService;
    }

    @PostMapping(value = "/process", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Ses dosyasını işleyip asistan cevabı üretir",
            description = "multipart/form-data ile ses dosyasını alır, transcript üretir, mevcut chat memory ile cevabı oluşturur ve ses çıktısı döner."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Başarılı işlem",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = VoiceAssistantResponse.class))),
            @ApiResponse(responseCode = "400", description = "Geçersiz istek"),
            @ApiResponse(responseCode = "401", description = "Yetkisiz"),
            @ApiResponse(responseCode = "500", description = "İşleme hatası")
    })
    @Override
    public RootEntity<VoiceAssistantResponse> processAudio(
            @Parameter(description = "Ses dosyası (mp3/wav)", required = true)
            @org.springframework.web.bind.annotation.RequestPart("audio") MultipartFile audio,
            @Parameter(description = "Konuşma bağlamı için opsiyonel id")
            @RequestParam(value = "conversationId", required = false) String conversationId,
            HttpSession session) {
        String effectiveConversationId = resolveConversationId(conversationId, session);
        log.info("voice process endpoint hit, conversationId={}, originalFilename={}, size={}",
                effectiveConversationId,
                audio.getOriginalFilename(),
                audio.getSize());

        VoiceAssistantResponse response = voiceAssistantService.processAudio(toResource(audio), effectiveConversationId);
        return ok(response);
    }

    @GetMapping(value = "/audio/{conversationId}", produces = "audio/mpeg")
    @Operation(
            summary = "İlgili konuşma için üretilen mp3 sesini döner",
            description = "En son üretilen asistan sesini raw mp3 byte stream olarak döner."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ses stream edildi"),
            @ApiResponse(responseCode = "401", description = "Yetkisiz"),
            @ApiResponse(responseCode = "404", description = "Ses bulunamadı")
    })
    @Override
    public ResponseEntity<byte[]> getAudio(
            @Parameter(description = "Konuşma id'si", required = true)
            @org.springframework.web.bind.annotation.PathVariable String conversationId) {
        byte[] audio = voiceAssistantService.getAudio(conversationId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"speech.mp3\"")
                .body(audio);
    }

    @DeleteMapping("/conversation")
    @Operation(
            summary = "Konuşma hafızasını temizler",
            description = "Verilen conversationId için chat memory kaydını temizler. conversationId verilmezse session içindeki id kullanılır."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Konuşma temizlendi"),
            @ApiResponse(responseCode = "401", description = "Yetkisiz")
    })
    @Override
    public RootEntity<String> clearConversation(
            @Parameter(description = "Temizlenecek konuşma id'si")
            @RequestParam(value = "conversationId", required = false) String conversationId,
            HttpSession session) {
        String effectiveConversationId = resolveConversationId(conversationId, session);
        voiceAssistantService.clearConversation(effectiveConversationId);
        return ok("conversation cleared");
    }

    private String resolveConversationId(String conversationId, HttpSession session) {
        if (conversationId != null && !conversationId.isBlank()) {
            return conversationId.trim();
        }
        String sessionConversationId = (String) session.getAttribute("VOICE_CONVERSATION_ID");
        if (sessionConversationId == null || sessionConversationId.isBlank()) {
            sessionConversationId = UUID.randomUUID().toString();
            session.setAttribute("VOICE_CONVERSATION_ID", sessionConversationId);
        }
        return sessionConversationId;
    }

    private Resource toResource(MultipartFile audio) {
        try {
            byte[] bytes = audio.getBytes();
            String filename = audio.getOriginalFilename() == null ? "audio" : audio.getOriginalFilename();
            return new ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    return filename;
                }
            };
        } catch (IOException e) {
            throw new BaseException(new ErrorMessage(MessageType.VOICE_ASSISTANT_FAILED, e.getMessage()));
        }
    }
}
