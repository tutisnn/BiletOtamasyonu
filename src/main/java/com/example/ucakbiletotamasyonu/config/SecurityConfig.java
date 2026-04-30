package com.example.ucakbiletotamasyonu.config;

import com.example.ucakbiletotamasyonu.handler.AuthEntryPoint;
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
    public static final String REGISTER = AUTH_BASE + "/register";
    public static final String LOGIN = AUTH_BASE + "/login";
    public static final String REFRESH_TOKEN = AUTH_BASE + "/refresh-token";
    public static final String LOGOUT = AUTH_BASE + "/logout";



    @Autowired
    private AuthenticationProvider authenticationProvider;
    @Autowired
    private JWTAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private AuthEntryPoint authEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(REGISTER)).permitAll()
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(LOGIN)).permitAll()
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(REFRESH_TOKEN)).permitAll()
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(LOGOUT)).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

