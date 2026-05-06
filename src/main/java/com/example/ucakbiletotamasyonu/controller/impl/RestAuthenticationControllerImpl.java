package com.example.ucakbiletotamasyonu.controller.impl;
import com.example.ucakbiletotamasyonu.controller.IRestAuthenticationController;
import com.example.ucakbiletotamasyonu.controller.RestBaseController;
import com.example.ucakbiletotamasyonu.controller.RootEntity;
import com.example.ucakbiletotamasyonu.dto.AuthRequest;
import com.example.ucakbiletotamasyonu.dto.AuthResponse;
import com.example.ucakbiletotamasyonu.dto.DtoUser;
import com.example.ucakbiletotamasyonu.dto.ResendVerificationEmailRequest;
import com.example.ucakbiletotamasyonu.dto.VerifyEmailRequest;
import com.example.ucakbiletotamasyonu.service.IAuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/auth")
public class RestAuthenticationControllerImpl extends RestBaseController implements IRestAuthenticationController {
    private static final Logger log = LoggerFactory.getLogger(RestAuthenticationControllerImpl.class);

    private final IAuthenticationService authenticationService;

    public RestAuthenticationControllerImpl(IAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    @Override
    public RootEntity<DtoUser> register(@Valid @RequestBody AuthRequest input) {
        return ok(authenticationService.register(input));
    }

    @PostMapping("/verify-email")
    @Override
    public RootEntity<DtoUser> verifyEmail(@Valid @RequestBody VerifyEmailRequest input) {
        return ok(authenticationService.verifyEmail(input));
    }

    @PostMapping("/resend-verification-email")
    @Override
    public RootEntity<String> resendVerificationEmail(@Valid @RequestBody ResendVerificationEmailRequest input) {
        log.info("resend-verification-email hit for email={}", input.getEmail());
        authenticationService.resendVerificationEmail(input);
        return ok("verification email resent");
    }

    @PostMapping("/login")
    @Override
    public RootEntity<AuthResponse> authenticate(@Valid @RequestBody AuthRequest input, HttpServletResponse response) {
        return ok(authenticationService.authenticate(input, response));
    }

    @PostMapping("/refresh-token")
    @Override
    public RootEntity<AuthResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        return ok(authenticationService.refreshToken(request, response));
    }

    @PostMapping("/logout")
    @Override
    public RootEntity<String> logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        authenticationService.logout(request, response, authentication);
        return ok("logout successful");
    }
}

