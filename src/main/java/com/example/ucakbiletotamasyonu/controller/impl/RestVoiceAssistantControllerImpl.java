package com.example.ucakbiletotamasyonu.controller.impl;

import com.example.ucakbiletotamasyonu.controller.IRestVoiceAssistantController;
import com.example.ucakbiletotamasyonu.controller.RestBaseController;
import com.example.ucakbiletotamasyonu.controller.RootEntity;
import com.example.ucakbiletotamasyonu.dto.VoiceAssistantResponse;
import com.example.ucakbiletotamasyonu.exception.BaseException;
import com.example.ucakbiletotamasyonu.exception.ErrorMessage;
import com.example.ucakbiletotamasyonu.exception.MessageType;
import com.example.ucakbiletotamasyonu.service.IVoiceAssistantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/voice")
@Tag(
        name = "Voice Assistant",
        description = "Sesli asistan ses yukleme, transcribe etme ve cevap uretme endpointleri"
)
public class RestVoiceAssistantControllerImpl extends RestBaseController implements IRestVoiceAssistantController {

    private static final Logger log = LoggerFactory.getLogger(RestVoiceAssistantControllerImpl.class);

    private final IVoiceAssistantService voiceAssistantService;

    public RestVoiceAssistantControllerImpl(IVoiceAssistantService voiceAssistantService) {
        this.voiceAssistantService = voiceAssistantService;
    }

    @PostMapping(value = "/process", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Ses dosyasini isleyip asistan cevabi uretir",
            description = "multipart/form-data ile ses dosyasini alir, transcript uretir, cevap uretir ve ses cikti dondurur. Sunucuda konusma hafizasi tutulmaz; her istek bagimsizdir."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Basarili islem",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = VoiceAssistantResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Gecersiz istek"),
            @ApiResponse(responseCode = "401", description = "Yetkisiz"),
            @ApiResponse(responseCode = "500", description = "Isleme hatasi")
    })
    @Override
    public RootEntity<VoiceAssistantResponse> processAudio(
            @Parameter(description = "Ses dosyasi (mp3/wav)", required = true)
            @RequestPart("audio") MultipartFile audio
    ) {
        log.info("voice process endpoint hit, originalFilename={}, size={}",
                audio.getOriginalFilename(),
                audio.getSize());

        VoiceAssistantResponse response = voiceAssistantService.processAudio(toResource(audio));
        return ok(response);
    }

    @GetMapping(value = "/audio/{conversationId}", produces = "audio/mpeg")
    @Operation(
            summary = "Ilgili istek icin uretilen mp3 sesini doner",
            description = "Uretilen asistan sesini raw mp3 byte stream olarak doner."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ses stream edildi"),
            @ApiResponse(responseCode = "401", description = "Yetkisiz"),
            @ApiResponse(responseCode = "404", description = "Ses bulunamadi")
    })
    @Override
    public ResponseEntity<byte[]> getAudio(
            @Parameter(description = "Istek id'si (process cevabindaki conversationId)", required = true)
            @PathVariable String conversationId
    ) {
        byte[] audio = voiceAssistantService.getAudio(conversationId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"speech.mp3\"")
                .body(audio);
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

