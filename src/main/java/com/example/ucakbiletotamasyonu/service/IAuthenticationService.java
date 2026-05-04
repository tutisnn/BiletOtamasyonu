package com.example.ucakbiletotamasyonu.service;

import com.example.ucakbiletotamasyonu.dto.AuthRequest;
import com.example.ucakbiletotamasyonu.dto.AuthResponse;
import com.example.ucakbiletotamasyonu.dto.DtoUser;
import com.example.ucakbiletotamasyonu.dto.VerifyEmailRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface IAuthenticationService {

    public DtoUser register(AuthRequest input);
    public DtoUser verifyEmail(VerifyEmailRequest input);
    public AuthResponse authenticate(AuthRequest input, HttpServletResponse response);
    public AuthResponse googleLogin(OAuth2User oAuth2User, HttpServletResponse response);
    public AuthResponse facebookLogin(OAuth2User oAuth2User, HttpServletResponse response);

    public AuthResponse refreshToken(HttpServletRequest request, HttpServletResponse response);
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication);
}

