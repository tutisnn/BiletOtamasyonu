package com.example.ucakbiletotamasyonu.config;

import com.example.ucakbiletotamasyonu.handler.AuthEntryPoint;
import com.example.ucakbiletotamasyonu.handler.OAuth2LoginSuccessHandler;
import com.example.ucakbiletotamasyonu.jwt.JWTAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

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
    public static final String CHAT_BASE = "/api/v1/chat";
    public static final String PAYMENT_BASE = "/api/payments";
    public static final String FLIGHT_SEARCH = "/api/flights/search";
    public static final String FLIGHT_SAVE = "/api/flights/save";



    @Autowired
    private AuthenticationProvider authenticationProvider;
    @Autowired
    private JWTAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private AuthEntryPoint authEntryPoint;

    @Autowired
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Value("#{'${app.cors.allowed-origin-patterns:http://localhost:*,http://127.0.0.1:*,http://[::1]:*}'.split(',')}")
    private List<String> allowedOriginPatterns;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedOriginPatterns(
                allowedOriginPatterns.stream()
                        .map(String::trim)
                        .filter(pattern -> !pattern.isBlank())
                        .toList()
        );
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(Arrays.asList("Set-Cookie", "Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(SWAGGER_UI)).permitAll()
                    .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(SWAGGER_UI_HTML)).permitAll()
                    .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(API_DOCS)).permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(REGISTER)).permitAll()
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(VERIFY_EMAIL)).permitAll()
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(RESEND_VERIFICATION_EMAIL)).permitAll()
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(PASSWORD_RESET_REQUEST)).permitAll()
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(PASSWORD_RESET)).permitAll()
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(LOGIN)).permitAll()
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(REFRESH_TOKEN)).permitAll()
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(LOGOUT)).permitAll()
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(CHAT_BASE + "/**")).permitAll()
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(VOICE_BASE + "/**")).permitAll()
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(FLIGHT_SEARCH)).permitAll()
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(FLIGHT_SAVE)).permitAll()
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(PAYMENT_BASE + "/success")).permitAll()
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(PAYMENT_BASE + "/cancel")).permitAll()
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher("/oauth2/**")).permitAll()
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher("/login/oauth2/**")).permitAll()
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher("/error")).permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2.successHandler(oAuth2LoginSuccessHandler))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
