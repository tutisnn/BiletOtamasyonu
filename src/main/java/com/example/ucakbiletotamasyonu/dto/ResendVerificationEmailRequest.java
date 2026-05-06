package com.example.ucakbiletotamasyonu.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResendVerificationEmailRequest {

    @NotBlank
    @Email
    private String email;
}
