package com.example.ucakbiletotamasyonu.jwt;

import com.example.ucakbiletotamasyonu.exception.BaseException;
import com.example.ucakbiletotamasyonu.exception.ErrorMessage;
import com.example.ucakbiletotamasyonu.exception.MessageType;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JWTAuthenticationFilter.class);

    @Autowired
    JwtService jwtService;
    @Autowired
    UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("JWT filter hit: method={}, uri={}, authHeaderPresent={}",
                request.getMethod(),
                request.getRequestURI(),
                request.getHeader("Authorization") != null);

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            if (header != null) {
                log.info("JWT filter skipped parsing because Authorization header is not Bearer: {}", header);
            }
            filterChain.doFilter(request, response);
            return;
        }
        String token = header.substring(7);
        log.info("JWT filter bearer token detected, tokenLength={}", token.length());

        try {
            String username = jwtService.getUsernameByToken(token);
            log.info("JWT token parsed username={}", username);
            if (username!=null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (userDetails != null && jwtService.isTokenValid(token)) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken.setDetails(userDetails);
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    log.info("JWT authentication set for username={}", username);
                }

            }
        }
        catch (UsernameNotFoundException e) {
            // If the client sends a stale token (user deleted etc.), do not fail public endpoints with 500.
            // Leave request unauthenticated; SecurityConfig will enforce auth only where required.
            SecurityContextHolder.clearContext();
            log.warn("JWT filter user not found on uri={}, username={}", request.getRequestURI(), e.getMessage());
        }
        catch (JwtException e) {
            // Invalid/expired token: continue without authentication.
            SecurityContextHolder.clearContext();
            log.warn("JWT filter jwt exception on uri={}: {}", request.getRequestURI(), e.getMessage());
        }
        catch (Exception e) {
            // Never break the request pipeline here; auth is enforced by Spring Security where needed.
            SecurityContextHolder.clearContext();
            log.warn("JWT filter general exception on uri={}: {}", request.getRequestURI(), e.getMessage());
        }
        filterChain.doFilter(request, response);

    }
}

