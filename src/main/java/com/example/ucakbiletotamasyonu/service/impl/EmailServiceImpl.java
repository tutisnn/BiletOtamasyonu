package com.example.ucakbiletotamasyonu.service.impl;

import com.example.ucakbiletotamasyonu.service.IEmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements IEmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendVerificationCode(String toEmail, String verificationCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        if (fromAddress != null && !fromAddress.isBlank()) {
            message.setFrom(fromAddress);
        }
        message.setSubject("UcakBiletOtamasyonu account verification");
        message.setText("""
                Hello,

                Your verification code is: %s

                This code is valid for 24 hours.

                If you did not request this email, you can ignore it.
                """.formatted(verificationCode));
        mailSender.send(message);
    }

    @Override
    public void sendPasswordResetToken(String toEmail, String passwordResetToken) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        if (fromAddress != null && !fromAddress.isBlank()) {
            message.setFrom(fromAddress);
        }
        message.setSubject("UcakBiletOtamasyonu password reset request");
        message.setText("""
                Hello,

                A password reset was requested for your account.

                Use this token to reset your password:
                %s

                This token is valid for 4 hours.

                If you did not request a password reset, please ignore this email.
                """.formatted(passwordResetToken));
        mailSender.send(message);
    }
}
