package com.example.monoauction.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtUtil;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = request.getHeader("Authorization");

            if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
                Claims claims = jwtUtil.getClaims(token.substring(7));

                if (claims.get("userId") == null || claims.get("role") == null) {
                    throw new JwtException("Missing required token claims");
                }

                CustomPrincipal customPrincipal = new CustomPrincipal(claims.get("userId").toString(), claims.getSubject(), claims.get("role").toString());

                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(claims.get("role").toString());
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(customPrincipal, null, Collections.singleton(authority));
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        filterChain.doFilter(request, response);
    }
}
