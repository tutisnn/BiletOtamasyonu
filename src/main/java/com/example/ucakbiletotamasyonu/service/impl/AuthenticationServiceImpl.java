package com.example.ucakbiletotamasyonu.service.impl;

import com.example.ucakbiletotamasyonu.dto.AuthRequest;
import com.example.ucakbiletotamasyonu.dto.AuthResponse;
import com.example.ucakbiletotamasyonu.dto.DtoUser;
import com.example.ucakbiletotamasyonu.dto.VerifyEmailRequest;
import com.example.ucakbiletotamasyonu.exception.BaseException;
import com.example.ucakbiletotamasyonu.exception.ErrorMessage;
import com.example.ucakbiletotamasyonu.exception.MessageType;
import com.example.ucakbiletotamasyonu.jwt.JwtService;
import com.example.ucakbiletotamasyonu.model.AuthProvider;
import com.example.ucakbiletotamasyonu.model.RefreshToken;
import com.example.ucakbiletotamasyonu.model.User;
import com.example.ucakbiletotamasyonu.repository.RefreshTokenRepository;
import com.example.ucakbiletotamasyonu.repository.UserRepository;
import com.example.ucakbiletotamasyonu.service.IAuthenticationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Optional;
import java.security.SecureRandom;
import java.util.UUID;
@Service
public class AuthenticationServiceImpl implements IAuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationServiceImpl.class);
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final long REFRESH_TOKEN_MAX_AGE_SECONDS = 60 * 60 * 4;
    private static final long VERIFICATION_CODE_MAX_AGE_MILLIS = 15 * 60 * 1000L;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

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
    private AuthenticationProvider authenticationProvider;
    @Autowired
    private JwtService jwtService;

    @Value("${security.cookie.secure:false}")
    private boolean secureCookie;




    private User createUser(AuthRequest input) {
        User user = new User();
        user.setCreateTime(new Date());
        user.setEmail(input.getEmail());
        user.setPassword(passwordEncoder.encode(input.getPassword()));
        user.setProvider(AuthProvider.LOCAL);
        user.setEmailVerified(false);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);

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
        user.setEmailVerified(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        return user;
    }

    private String generateVerificationCode() {
        int code = 100000 + SECURE_RANDOM.nextInt(900000);
        return String.valueOf(code);
    }

    private void issueVerificationCode(User user) {
        String verificationCode = generateVerificationCode();
        user.setEmailVerified(false);
        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpiresAt(new Date(System.currentTimeMillis() + VERIFICATION_CODE_MAX_AGE_MILLIS));
        userRepository.save(user);
        log.info("Verification code for {} is {}", user.getEmail(), verificationCode);
    }

    private boolean isEmailVerified(User user) {
        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            return true;
        }
        return user.getEmailVerified() == null
                && user.getVerificationCode() == null
                && user.getVerificationCodeExpiresAt() == null;
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setCreateTime(new Date());
        refreshToken.setExpiredDate(new Date(System.currentTimeMillis() + 1000*60*60*4));
        refreshToken.setRefreshToken(UUID.randomUUID().toString());
        refreshToken.setUser(user);
        return refreshToken;
    }

    @Override
    public DtoUser register(AuthRequest input) {
        DtoUser dtoUser = new DtoUser();

        if (userRepository.findByEmail(input.getEmail()).isPresent()) {
            throw new BaseException(new ErrorMessage(MessageType.EMAIL_ALREADY_REGISTERED, input.getEmail()));
        }

        User savedUser = userRepository.save(createUser(input));
        issueVerificationCode(savedUser);

        BeanUtils.copyProperties(savedUser, dtoUser);
        return dtoUser;
    }

    @Override
    public DtoUser verifyEmail(VerifyEmailRequest input) {
        Optional<User> optionalUser = userRepository.findByEmail(input.getEmail());
        if (optionalUser.isEmpty()) {
            throw new BaseException(new ErrorMessage(MessageType.EMAIL_NOT_FOUND, input.getEmail()));
        }

        User user = optionalUser.get();
        if (user.getProvider() != AuthProvider.LOCAL) {
            throw new BaseException(new ErrorMessage(MessageType.EMAIL_ALREADY_REGISTERED, input.getEmail()));
        }

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            DtoUser dtoUser = new DtoUser();
            BeanUtils.copyProperties(user, dtoUser);
            return dtoUser;
        }

        if (user.getVerificationCodeExpiresAt() == null || new Date().after(user.getVerificationCodeExpiresAt())) {
            throw new BaseException(new ErrorMessage(MessageType.VERIFICATION_CODE_EXPIRED, input.getEmail()));
        }

        if (!input.getVerificationCode().equals(user.getVerificationCode())) {
            throw new BaseException(new ErrorMessage(MessageType.VERIFICATION_CODE_INVALID, input.getVerificationCode()));
        }

        user.setEmailVerified(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        User savedUser = userRepository.save(user);

        DtoUser dtoUser = new DtoUser();
        BeanUtils.copyProperties(savedUser, dtoUser);
        return dtoUser;
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
            if (!isEmailVerified(optUser.get())) {
                throw new BaseException(new ErrorMessage(MessageType.EMAIL_NOT_VERIFIED, input.getEmail()));
            }

            String accessToken = jwtService.generateToken(optUser.get());
            RefreshToken savedRefreshToken = refreshTokenRepository.save(createRefreshToken(optUser.get()));
            addRefreshTokenCookie(response, savedRefreshToken.getRefreshToken());

            return new AuthResponse(accessToken);
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
