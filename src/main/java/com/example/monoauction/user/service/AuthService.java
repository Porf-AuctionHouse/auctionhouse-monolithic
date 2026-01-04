package com.example.monoauction.user.service;



import com.example.monoauction.common.execptions.AuctionHouseException;
import com.example.monoauction.user.dto.LoginRequest;
import com.example.monoauction.user.dto.RegisterRequest;
import com.example.monoauction.user.model.User;
import com.example.monoauction.common.enums.ErrorMessage;
import com.example.monoauction.user.dto.Token;
import com.example.monoauction.user.repository.UserRepository;
import com.example.monoauction.security.JwtTokenProvider;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private final AuthenticationManager authenticationManager;

    @Autowired
    private final JwtTokenProvider jwtTokenProvider;

    public String register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuctionHouseException(ErrorMessage.EMAIL_ALREADY_EXISTS);
        }
        User appUsers = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole())
                .build();

        User savedUser = userRepository.save(appUsers);

        return "User registered successfully";
    }


    public @Nullable Token login(@Valid LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(() -> new AuctionHouseException(ErrorMessage.USER_NOT_FOUND));
        log.info("User found: {}", user);
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
            log.info("Authentication successful: {}", authentication);
        } catch (AuthenticationException e) {
            log.error("Authentication failed: {}", e.getMessage());
            throw new AuctionHouseException(ErrorMessage.INVALID_CREDENTIALS);
        }


        return generateToken(loginRequest.getEmail());
    }

    public @Nullable Token refreshToken(String token) {
        if(jwtTokenProvider.validateToken(token)){
            String username = jwtTokenProvider.extractUsername(token);
            return generateToken(username);
        }
        throw new JwtException("Invalid token");
    }


    private Token generateToken(String username) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime accessExpiry = now.plus(jwtTokenProvider.getAccessTokenExpirationInMillis(),
                java.time.temporal.ChronoUnit.MILLIS);
        LocalDateTime refreshExpiry = now.plus(jwtTokenProvider.getRefreshTokenExpirationInMillis(),
                java.time.temporal.ChronoUnit.MILLIS);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");
        return Token.builder()
                .accessToken(jwtTokenProvider.generateAccessToken(username))
                .refreshToken(jwtTokenProvider.generateRefreshToken(username))
                .accessExpirationTime(accessExpiry.format(formatter))
                .refreshExpirationTime(refreshExpiry.format(formatter))
                .build();
    }
}
