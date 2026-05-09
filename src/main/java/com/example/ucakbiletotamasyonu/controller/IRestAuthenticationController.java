package com.example.ucakbiletotamasyonu.controller;

import com.example.ucakbiletotamasyonu.dto.AuthRequest;
import com.example.ucakbiletotamasyonu.dto.AuthResponse;
import com.example.ucakbiletotamasyonu.dto.UserDto;
import com.example.ucakbiletotamasyonu.dto.PasswordResetRequest;
import com.example.ucakbiletotamasyonu.dto.ResendVerificationEmailRequest;
import com.example.ucakbiletotamasyonu.dto.ResetPasswordRequest;
import com.example.ucakbiletotamasyonu.dto.VerifyEmailRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;

public interface IRestAuthenticationController {

    public RootEntity<UserDto> register(AuthRequest input);
    public RootEntity<UserDto> verifyEmail(VerifyEmailRequest input);
    public RootEntity<String> resendVerificationEmail(ResendVerificationEmailRequest input);
    public RootEntity<String> requestPasswordReset(PasswordResetRequest input);
    public RootEntity<String> resetPassword(ResetPasswordRequest input);
    public RootEntity<AuthResponse> authenticate(AuthRequest input, HttpServletResponse response);
    public RootEntity<AuthResponse> refreshToken(HttpServletRequest request, HttpServletResponse response);
    public RootEntity<String> logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication);
}

