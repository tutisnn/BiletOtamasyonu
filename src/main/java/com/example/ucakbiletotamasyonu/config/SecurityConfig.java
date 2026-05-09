package com.example.ucakbiletotamasyonu.config;

import com.example.ucakbiletotamasyonu.handler.AuthEntryPoint;
import com.example.ucakbiletotamasyonu.handler.OAuth2LoginSuccessHandler;
import com.example.ucakbiletotamasyonu.jwt.JWTAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
public class SecurityConfig {

    public static final String AUTH_BASE = "/api/v1/auth";
    public static final String SWAGGER_UI = "/swagger-ui/**";
    public static final String SWAGGER_UI_HTML = "/swagger-ui.html";
    public static final String API_DOCS = "/v3/api-docs/**";
    public static final String REGISTER = AUTH_BASE + "/register";
    public static final String VERIFY_EMAIL = AUTH_BASE + "/verify-email";
    public static final String RESEND_VERIFICATION_EMAIL = AUTH_BASE + "/resend-verification-email";
    public static final String PASSWORD_RESET_REQUEST = AUTH_BASE + "/password-reset-request";
    public static final String PASSWORD_RESET = AUTH_BASE + "/password-reset";
    public static final String LOGIN = AUTH_BASE + "/login";
    public static final String REFRESH_TOKEN = AUTH_BASE + "/refresh-token";
    public static final String LOGOUT = AUTH_BASE + "/logout";
    public static final String VOICE_BASE = "/api/v1/voice";



    @Autowired
    private AuthenticationProvider authenticationProvider;
    @Autowired
    private JWTAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private AuthEntryPoint authEntryPoint;

    @Autowired
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(SWAGGER_UI)).permitAll()
                    .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(SWAGGER_UI_HTML)).permitAll()
                    .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(API_DOCS)).permitAll()
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(REGISTER)).permitAll()
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(VERIFY_EMAIL)).permitAll()
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(RESEND_VERIFICATION_EMAIL)).permitAll()
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(PASSWORD_RESET_REQUEST)).permitAll()
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(PASSWORD_RESET)).permitAll()
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(LOGIN)).permitAll()
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(REFRESH_TOKEN)).permitAll()
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(LOGOUT)).permitAll()
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(VOICE_BASE + "/**")).authenticated()
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher("/oauth2/**")).permitAll()
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher("/login/oauth2/**")).permitAll()
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher("/error")).permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2.successHandler(oAuth2LoginSuccessHandler))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
