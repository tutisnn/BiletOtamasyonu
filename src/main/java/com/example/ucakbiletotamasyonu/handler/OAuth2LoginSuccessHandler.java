package com.example.ucakbiletotamasyonu.handler;

import com.example.ucakbiletotamasyonu.controller.RootEntity;
import com.example.ucakbiletotamasyonu.dto.AuthResponse;
import com.example.ucakbiletotamasyonu.exception.BaseException;
import com.example.ucakbiletotamasyonu.exception.ErrorMessage;
import com.example.ucakbiletotamasyonu.exception.MessageType;
import com.example.ucakbiletotamasyonu.service.IAuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final IAuthenticationService authenticationService;
    private final ObjectMapper objectMapper;

    public OAuth2LoginSuccessHandler(IAuthenticationService authenticationService, ObjectMapper objectMapper) {
        this.authenticationService = authenticationService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        if (!(authentication instanceof OAuth2AuthenticationToken oAuth2AuthenticationToken)) {
            throw new BaseException(new ErrorMessage(MessageType.GENERAL_EXCEPTION, "oauth2 authentication not found"));
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof org.springframework.security.oauth2.core.user.OAuth2User oAuth2User)) {
            throw new BaseException(new ErrorMessage(MessageType.GENERAL_EXCEPTION, "oauth2 principal not found"));
        }

        String registrationId = oAuth2AuthenticationToken.getAuthorizedClientRegistrationId();
        AuthResponse authResponse;
        if ("google-login".equals(registrationId)) {
            authResponse = authenticationService.googleLogin(oAuth2User, response);
        } else if ("facebook-login".equals(registrationId)) {
            authResponse = authenticationService.facebookLogin(oAuth2User, response);
        } else {
            throw new BaseException(new ErrorMessage(MessageType.GENERAL_EXCEPTION, "unsupported oauth2 registration"));
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), RootEntity.ok(authResponse));
    }
}
