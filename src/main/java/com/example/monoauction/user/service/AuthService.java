package com.example.monoauction.user.service;



import com.example.monoauction.common.enums.UserRole;
import com.example.monoauction.user.dto.Token;
import com.example.monoauction.user.model.User;
import com.example.monoauction.security.JwtTokenProvider;
import io.jsonwebtoken.JwtException;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    @Autowired
    private final UserService userService;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private final AuthenticationManager authenticationManager;

    @Autowired
    private final JwtTokenProvider tokenProvider;

    public Token login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userService.getUserByEmail(email);
        user.setLastLoginAt(LocalDateTime.now());
        User savedUser = userService.saveUser(user);
        return generateToken(user);

    }

    public User register(String email, String password, String fullName, UserRole role) {
        return userService.registerUser(email, password, fullName, role);
    }

    public User getUserByEmail(String email){
        return userService.getUserByEmail(email);
    }

    public Token refreshToken(String token) {
        if(tokenProvider.validateToken(token)){
            Long userId = tokenProvider.getUserIdFromToken(token);
            User user = userService.getUserById(userId);
            return generateToken(user);
        }
        throw new JwtException("Invalid token");
    }


    private Token generateToken(User user) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime accessExpiry = now.plus(tokenProvider.getAccessTokenExpirationInMillis(),
                java.time.temporal.ChronoUnit.MILLIS);
        LocalDateTime refreshExpiry = now.plus(tokenProvider.getRefreshTokenExpirationInMillis(),
                java.time.temporal.ChronoUnit.MILLIS);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");
        return Token.builder()
                .accessToken(tokenProvider.generateAccessToken(user))
                .refreshToken(tokenProvider.generateRefreshToken(user))
                .accessExpirationTime(accessExpiry.format(formatter))
                .refreshExpirationTime(refreshExpiry.format(formatter))
                .build();
    }
}
