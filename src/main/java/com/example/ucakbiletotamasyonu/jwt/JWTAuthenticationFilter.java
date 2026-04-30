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
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    JwtService jwtService;
    @Autowired
    UserDetailsService userDetailsService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null ) {
            filterChain.doFilter(request, response);
            return;
        }
        String token;
        String username;
        token=header.substring(7);

        try {
            username=jwtService.getUsernameByToken(token);
            if (username!=null && SecurityContextHolder.getContext().getAuthentication() == null) {
             UserDetails userDetails= userDetailsService.loadUserByUsername(username);
             if(userDetails!=null && jwtService.isTokenValid(token)) {
                 UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken=new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                 usernamePasswordAuthenticationToken.setDetails(userDetails);
                 SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
             }

            }





        }
        catch (JwtException e) {
            throw new BaseException(new ErrorMessage(MessageType.TOKEN_IS_EXPIRED, e.getMessage()));
        }
        catch (Exception e) {
            throw new BaseException(new ErrorMessage(MessageType.GENERAL_EXCEPTION, e.getMessage()));
        }
        filterChain.doFilter(request, response);

    }
}

