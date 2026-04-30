package com.example.ucakbiletotamasyonu.service;

import com.example.ucakbiletotamasyonu.dto.AuthRequest;
import com.example.ucakbiletotamasyonu.dto.AuthResponse;
import com.example.ucakbiletotamasyonu.dto.DtoUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;

public interface IAuthenticationService {

    public DtoUser register(AuthRequest input);
    public AuthResponse authenticate(AuthRequest input, HttpServletResponse response);

    public AuthResponse refreshToken(HttpServletRequest request, HttpServletResponse response);
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication);
}

