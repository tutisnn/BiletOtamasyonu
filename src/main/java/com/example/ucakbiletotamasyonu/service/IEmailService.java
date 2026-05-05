package com.example.ucakbiletotamasyonu.service;

public interface IEmailService {

    void sendVerificationCode(String toEmail, String verificationCode);
}
