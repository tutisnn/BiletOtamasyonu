package com.example.ucakbiletotamasyonu.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Some clients (or users) hit "/login" directly.
 * This project does not serve a login page, so we redirect to the OAuth2 authorization endpoint.
 */
@Controller
public class AuthRedirectController {

    @GetMapping("/login")
    public String login() {
        // registrationId in application.yml: spring.security.oauth2.client.registration.google-login
        return "redirect:/oauth2/authorization/google-login";
    }
}

