package com.example.ucakbiletotamasyonu.service.impl;

import com.example.ucakbiletotamasyonu.dto.AuthRequest;
import com.example.ucakbiletotamasyonu.dto.AuthResponse;
import com.example.ucakbiletotamasyonu.dto.UserDto;
import com.example.ucakbiletotamasyonu.dto.PasswordResetRequest;
import com.example.ucakbiletotamasyonu.dto.ResendVerificationEmailRequest;
import com.example.ucakbiletotamasyonu.dto.ResetPasswordRequest;
import com.example.ucakbiletotamasyonu.dto.VerifyEmailRequest;
import com.example.ucakbiletotamasyonu.event.OnRegistrationCompleteEvent;
import com.example.ucakbiletotamasyonu.exception.BaseException;
import com.example.ucakbiletotamasyonu.exception.ErrorMessage;
import com.example.ucakbiletotamasyonu.exception.MessageType;
import com.example.ucakbiletotamasyonu.jwt.JwtService;
import com.example.ucakbiletotamasyonu.model.AuthProvider;
import com.example.ucakbiletotamasyonu.model.PasswordResetToken;
import com.example.ucakbiletotamasyonu.model.RefreshToken;
import com.example.ucakbiletotamasyonu.model.User;
import com.example.ucakbiletotamasyonu.repository.PasswordResetTokenRepository;
import com.example.ucakbiletotamasyonu.repository.RefreshTokenRepository;
import com.example.ucakbiletotamasyonu.repository.UserRepository;
import com.example.ucakbiletotamasyonu.repository.VerificationTokenRepository;
import com.example.ucakbiletotamasyonu.service.IAuthenticationService;
import com.example.ucakbiletotamasyonu.service.IEmailService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.authentication.DisabledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;
@Service
public class AuthenticationServiceImpl implements IAuthenticationService {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final long REFRESH_TOKEN_MAX_AGE_SECONDS = 60 * 60 * 4;

    /* ClearSiteDataHeaderWriter→ Spring Security'nin HTTP response'a Clear-Site-Data header'ı ekleyen sınıfı.
      learSiteDataHeaderWriter.Directive.COOKIESSadece cookie'leri temizle" direktifi
     */
    private static final ClearSiteDataHeaderWriter CLEAR_SITE_DATA_HEADER_WRITER =
            new ClearSiteDataHeaderWriter(ClearSiteDataHeaderWriter.Directive.COOKIES);


    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private VerificationTokenRepository verificationTokenRepository;
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private AuthenticationProvider authenticationProvider;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private IEmailService emailService;

    @Value("${security.cookie.secure:false}")
    private boolean secureCookie;

    private User createUser(AuthRequest input) {
        User user = new User();
        user.setCreateTime(new Date());
        user.setEmail(input.getEmail());
        user.setPassword(passwordEncoder.encode(input.getPassword()));
        user.setProvider(AuthProvider.LOCAL);
        user.setEnabled(false);

        return user;
    }

    private User createGoogleUser(String email) {
        return createSocialUser(email, AuthProvider.GOOGLE);
    }

    private User createSocialUser(String email, AuthProvider provider) {
        User user = new User();
        user.setCreateTime(new Date());
        user.setEmail(email);
        user.setPassword(null);
        user.setProvider(provider);
        user.setEnabled(true);
        return user;
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setCreateTime(new Date());
        refreshToken.setExpiredDate(new Date(System.currentTimeMillis() + 1000*60*60*4));
        refreshToken.setRefreshToken(UUID.randomUUID().toString());
        refreshToken.setUser(user);
        return refreshToken;
    }

    private PasswordResetToken createPasswordResetToken(User user) {
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setCreateTime(new Date());
        passwordResetToken.setExpiryDate(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 4));
        passwordResetToken.setToken(UUID.randomUUID().toString());
        passwordResetToken.setUser(user);
        return passwordResetToken;
    }

    @Override
    @Transactional
    public UserDto register(AuthRequest input) {
        UserDto userDto = new UserDto();

        if (userRepository.findByEmail(input.getEmail()).isPresent()) {
            throw new BaseException(new ErrorMessage(MessageType.EMAIL_ALREADY_REGISTERED, input.getEmail()));
        }

        User savedUser = userRepository.save(createUser(input));
        publishVerificationCodeEvent(savedUser);

        BeanUtils.copyProperties(savedUser, userDto);
        return userDto;
    }

    @Override
    @Transactional
    public void resendVerificationEmail(ResendVerificationEmailRequest input) {
        log.info("resendVerificationEmail service called for email={}", input.getEmail());
        User user = userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.EMAIL_NOT_FOUND, input.getEmail())));

        if (user.isEnabled()) {
            log.info("resendVerificationEmail rejected because already verified: {}", input.getEmail());
            throw new BaseException(new ErrorMessage(MessageType.EMAIL_ALREADY_VERIFIED, input.getEmail()));
        }

        verificationTokenRepository.deleteByUser(user);
        log.info("old verification tokens deleted for email={}", input.getEmail());
        publishVerificationCodeEvent(user);
    }

    @Override
    @Transactional
    public void requestPasswordReset(PasswordResetRequest input) {
        log.info("requestPasswordReset service called for email={}", input.getEmail());
        User user = userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.EMAIL_NOT_FOUND, input.getEmail())));

        if (user.getProvider() != AuthProvider.LOCAL) {
            throw new BaseException(new ErrorMessage(MessageType.PASSWORD_RESET_NOT_ALLOWED, input.getEmail()));
        }
        if (!user.isEnabled()) {
            throw new BaseException(new ErrorMessage(MessageType.EMAIL_NOT_VERIFIED, input.getEmail()));
        }

        passwordResetTokenRepository.deleteByUser(user);
        PasswordResetToken passwordResetToken = createPasswordResetToken(user);
        passwordResetTokenRepository.save(passwordResetToken);
        emailService.sendPasswordResetToken(user.getEmail(), passwordResetToken.getToken());
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest input) {
        var passwordResetToken = passwordResetTokenRepository.findByToken(input.getToken())
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.PASSWORD_RESET_TOKEN_INVALID, input.getToken())));

        if (passwordResetToken.getExpiryDate() == null || new Date().after(passwordResetToken.getExpiryDate())) {
            passwordResetTokenRepository.delete(passwordResetToken);
            throw new BaseException(new ErrorMessage(MessageType.PASSWORD_RESET_TOKEN_EXPIRED, input.getToken()));
        }

        User user = passwordResetToken.getUser();
        if (!user.getEmail().equalsIgnoreCase(input.getEmail())) {
            throw new BaseException(new ErrorMessage(MessageType.PASSWORD_RESET_TOKEN_INVALID, input.getToken()));
        }
        if (user.getProvider() != AuthProvider.LOCAL) {
            throw new BaseException(new ErrorMessage(MessageType.PASSWORD_RESET_NOT_ALLOWED, input.getEmail()));
        }

        user.setPassword(passwordEncoder.encode(input.getNewPassword()));
        userRepository.save(user);
        passwordResetTokenRepository.delete(passwordResetToken);
        refreshTokenRepository.deleteByUser(user);
    }

    @Override
    @Transactional
    public UserDto verifyEmail(VerifyEmailRequest input) {
        var verificationToken = verificationTokenRepository.findByToken(input.getVerificationCode())
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.VERIFICATION_CODE_INVALID, input.getVerificationCode())));

        if (verificationToken.getExpiryDate() == null || new Date().after(verificationToken.getExpiryDate())) {
            verificationTokenRepository.delete(verificationToken);
            throw new BaseException(new ErrorMessage(MessageType.VERIFICATION_CODE_EXPIRED, input.getVerificationCode()));
        }

        User user = verificationToken.getUser();
        if (!user.getEmail().equalsIgnoreCase(input.getEmail())) {
            throw new BaseException(new ErrorMessage(MessageType.VERIFICATION_CODE_INVALID, input.getVerificationCode()));
        }
        user.setEnabled(true);
        User savedUser = userRepository.save(user);
        verificationTokenRepository.delete(verificationToken);

        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(savedUser, userDto);
        return userDto;
    }

    @Override
    public AuthResponse authenticate(AuthRequest input, HttpServletResponse response) {
        try {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(input.getEmail(), input.getPassword());
            authenticationProvider.authenticate(authenticationToken);

            Optional<User> optUser = userRepository.findByEmail(input.getEmail());
            if (optUser.isEmpty() || optUser.get().getProvider() != AuthProvider.LOCAL) {
                throw new BaseException(new ErrorMessage(MessageType.EMAIL_OR_PASSWORD_INVALID, input.getEmail()));
            }
            if (!optUser.get().isEnabled()) {
                throw new BaseException(new ErrorMessage(MessageType.EMAIL_NOT_VERIFIED, input.getEmail()));
            }

            String accessToken = jwtService.generateToken(optUser.get());
            RefreshToken savedRefreshToken = refreshTokenRepository.save(createRefreshToken(optUser.get()));
            addRefreshTokenCookie(response, savedRefreshToken.getRefreshToken());

            return new AuthResponse(accessToken);
        } catch (DisabledException e) {
            throw new BaseException(new ErrorMessage(MessageType.EMAIL_NOT_VERIFIED, input.getEmail()));
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(new ErrorMessage(MessageType.EMAIL_OR_PASSWORD_INVALID, e.getMessage()));
        }
    }

    @Override
    public AuthResponse googleLogin(OAuth2User oAuth2User, HttpServletResponse response) {
        return socialLogin(oAuth2User, response, AuthProvider.GOOGLE);
    }

    @Override
    public AuthResponse facebookLogin(OAuth2User oAuth2User, HttpServletResponse response) {
        return socialLogin(oAuth2User, response, AuthProvider.FACEBOOK);
    }

    private AuthResponse socialLogin(OAuth2User oAuth2User, HttpServletResponse response, AuthProvider provider) {
        String email = oAuth2User.getAttribute("email");
        if (email == null || email.isBlank()) {
            throw new BaseException(new ErrorMessage(MessageType.EMAIL_NOT_FOUND, provider.name().toLowerCase() + " oauth2"));
        }

        Optional<User> optUser = userRepository.findByEmail(email);
        User user;
        if (optUser.isPresent()) {
            user = optUser.get();
            if (user.getProvider() != provider) {
                throw new BaseException(new ErrorMessage(MessageType.EMAIL_ALREADY_REGISTERED, email));
            }
        } else {
            user = userRepository.save(createSocialUser(email, provider));
        }

        String accessToken = jwtService.generateToken(user);
        RefreshToken savedRefreshToken = refreshTokenRepository.save(createRefreshToken(user));
        addRefreshTokenCookie(response, savedRefreshToken.getRefreshToken());

        return new AuthResponse(accessToken);
    }

    private void publishVerificationCodeEvent(User user) {
        log.info("publishing registration verification event for email={}", user.getEmail());
        applicationEventPublisher.publishEvent(new OnRegistrationCompleteEvent(user));
    }

    public boolean isValidRefreshToken(Date expiredDate) {
        return new Date().before(expiredDate);
    }
    @Override
    public AuthResponse refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshTokenValue = getRefreshTokenFromCookie(request);
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByRefreshToken(refreshTokenValue);
        if(refreshToken.isEmpty()) {
            throw new BaseException(new ErrorMessage(MessageType.REFRESH_TOKEN_NOT_FOUND, refreshTokenValue));
        }

        if(!isValidRefreshToken(refreshToken.get().getExpiredDate())) {
            throw new BaseException(new ErrorMessage(MessageType.REFRESH_TOKEN_IS_EXPIRED, refreshTokenValue));
        }
        User user = refreshToken.get().getUser();
        String accessToken = jwtService.generateToken(user);
        RefreshToken savedRefreshToken = refreshTokenRepository.save(createRefreshToken(user));
        addRefreshTokenCookie(response, savedRefreshToken.getRefreshToken());
        return new AuthResponse(accessToken);
    }

    @Override
    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String refreshTokenValue = getRefreshTokenFromCookieOrNull(request);
        if (refreshTokenValue != null) {
            refreshTokenRepository.deleteByRefreshToken(refreshTokenValue);
        }
        clearRefreshTokenCookie(response);
        CLEAR_SITE_DATA_HEADER_WRITER.writeHeaders(request, response);
        new SecurityContextLogoutHandler().logout(request, response, authentication);
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                // JavaScript'in cookie'ye erişimini engeller (document.cookie ile okunamaz)
                // XSS saldırılarına karşı koruma sağlar
                .httpOnly(true)

                // true  → Cookie sadece HTTPS üzerinden gönderilir (production)
                // false → HTTP'de de gönderilir (development)
                // application.properties'den gelir: security.cookie.secure=false
                .secure(secureCookie)

                // Cookie'nin hangi path'lere gönderileceğini belirler
                // Sadece /api/v1/auth/** isteklerinde cookie gönderilir
                // /api/v1/users gibi diğer endpoint'lere gönderilmez
                .path("/api/v1/auth")

                // Cookie'nin geçerlilik süresi (saniye cinsinden)
                // 60 * 60 * 4 = 4 saat
                // 0  → Cookie'yi hemen sil
                // -1 → Session cookie (tarayıcı kapanınca silinir)
                .maxAge(REFRESH_TOKEN_MAX_AGE_SECONDS)

                // Cross-site isteklerde cookie'nin gönderilip gönderilmeyeceğini belirler
                // "Strict" → Sadece aynı siteden gelen isteklerde gönderilir
                // "Lax"    → Aynı site + top-level GET isteklerinde gönderilir
                // "None"   → Her yerden gönderilir (Secure=true zorunlu olur)
                .sameSite("Lax")

                .build();

        // Cookie'yi response header'ına ekler
        // Set-Cookie: refreshToken=xxx; HttpOnly; Secure; Path=/api/v1/auth; ...
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secureCookie)
                .path("/api/v1/auth")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        String refreshTokenValue = getRefreshTokenFromCookieOrNull(request);
        if (refreshTokenValue == null) {
            throw new BaseException(new ErrorMessage(MessageType.REFRESH_TOKEN_NOT_FOUND, "cookie"));
        }
        return refreshTokenValue;
    }

    private String getRefreshTokenFromCookieOrNull(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                return cookie.getValue();
            }
        }
        return null;
    }

}
