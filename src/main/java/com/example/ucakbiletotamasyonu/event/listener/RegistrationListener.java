package com.example.ucakbiletotamasyonu.event.listener;

import com.example.ucakbiletotamasyonu.event.OnRegistrationCompleteEvent;
import com.example.ucakbiletotamasyonu.exception.BaseException;
import com.example.ucakbiletotamasyonu.exception.ErrorMessage;
import com.example.ucakbiletotamasyonu.exception.MessageType;
import com.example.ucakbiletotamasyonu.model.VerificationToken;
import com.example.ucakbiletotamasyonu.repository.VerificationTokenRepository;
import com.example.ucakbiletotamasyonu.service.IEmailService;
import java.util.Date;
import java.security.SecureRandom;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class RegistrationListener {

    private static final long VERIFICATION_TOKEN_MAX_AGE_MILLIS = 24 * 60 * 60 * 1000L;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final VerificationTokenRepository verificationTokenRepository;
    private final IEmailService emailService;

    public RegistrationListener(VerificationTokenRepository verificationTokenRepository, IEmailService emailService) {
        this.verificationTokenRepository = verificationTokenRepository;
        this.emailService = emailService;
    }

    @EventListener
    public void onRegistrationComplete(OnRegistrationCompleteEvent event) {
        String verificationCode = generateUniqueVerificationCode();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setCreateTime(new Date());
        verificationToken.setToken(verificationCode);
        verificationToken.setExpiryDate(new Date(System.currentTimeMillis() + VERIFICATION_TOKEN_MAX_AGE_MILLIS));
        verificationToken.setUser(event.getUser());

        verificationTokenRepository.save(verificationToken);

        try {
            emailService.sendVerificationCode(event.getUser().getEmail(), verificationCode);
        } catch (Exception e) {
            verificationTokenRepository.delete(verificationToken);
            throw new BaseException(new ErrorMessage(MessageType.VERIFICATION_EMAIL_SEND_FAILED, event.getUser().getEmail()));
        }
    }

    private String generateVerificationCode() {
        return String.valueOf(100000 + SECURE_RANDOM.nextInt(900000));
    }

    private String generateUniqueVerificationCode() {
        for (int i = 0; i < 10; i++) {
            String code = generateVerificationCode();
            if (verificationTokenRepository.findByToken(code).isEmpty()) {
                return code;
            }
        }
        throw new BaseException(new ErrorMessage(MessageType.GENERAL_EXCEPTION, "verification code generation failed"));
    }
}
