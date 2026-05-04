package com.example.ucakbiletotamasyonu.controller;

import com.example.ucakbiletotamasyonu.dto.AuthRequest;
import com.example.ucakbiletotamasyonu.dto.AuthResponse;
import com.example.ucakbiletotamasyonu.dto.DtoUser;
import com.example.ucakbiletotamasyonu.dto.VerifyEmailRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;

public interface IRestAuthenticationController {

    public RootEntity<DtoUser> register(AuthRequest input);
    public RootEntity<DtoUser> verifyEmail(VerifyEmailRequest input);
    public RootEntity<AuthResponse> authenticate(AuthRequest input, HttpServletResponse response);
    public RootEntity<AuthResponse> refreshToken(HttpServletRequest request, HttpServletResponse response);
    public RootEntity<String> logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication);
}

