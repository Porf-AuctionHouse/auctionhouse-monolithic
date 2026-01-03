package com.example.monoauction.authentication.service;



import com.example.monoauction.common.execptions.AuctionHouseException;
import com.example.monoauction.authentication.model.dto.LoginDto;
import com.example.monoauction.authentication.model.dto.RegisterDto;
import com.example.monoauction.authentication.model.entity.AppUsers;
import com.example.monoauction.authentication.model.enums.ErrorMessage;
import com.example.monoauction.authentication.model.enums.UserStatus;
import com.example.monoauction.authentication.model.payload.Token;
import com.example.monoauction.authentication.repository.UserRepository;
import com.example.monoauction.common.security.JwtService;
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
    private final JwtService jwtService;

    public String register(RegisterDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuctionHouseException(ErrorMessage.EMAIL_ALREADY_EXISTS);
        }
        AppUsers appUsers = AppUsers.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getDisplayName())
                .role(request.getRole())
                .status(UserStatus.UNVERIFIED)
                .build();

        AppUsers savedUser = userRepository.save(appUsers);

        return "User registered successfully";
    }


    public @Nullable Token login(@Valid LoginDto loginDto) {
        AppUsers user = userRepository.findByEmail(loginDto.getEmail()).orElseThrow(() -> new AuctionHouseException(ErrorMessage.USER_NOT_FOUND));
        log.info("User found: {}", user);
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDto.getEmail(),
                            loginDto.getPassword()
                    )
            );
            log.info("Authentication successful: {}", authentication);
        } catch (AuthenticationException e) {
            log.error("Authentication failed: {}", e.getMessage());
            throw new AuctionHouseException(ErrorMessage.INVALID_CREDENTIALS);
        }


        return generateToken(loginDto.getEmail());
    }

    public @Nullable Token refreshToken(String token) {
        if(jwtService.validateToken(token)){
            String username = jwtService.extractUsername(token);
            return generateToken(username);
        }
        throw new JwtException("Invalid token");
    }


    private Token generateToken(String username) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime accessExpiry = now.plus(jwtService.getAccessTokenExpirationInMillis(),
                java.time.temporal.ChronoUnit.MILLIS);
        LocalDateTime refreshExpiry = now.plus(jwtService.getRefreshTokenExpirationInMillis(),
                java.time.temporal.ChronoUnit.MILLIS);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");
        return Token.builder()
                .accessToken(jwtService.generateAccessToken(username))
                .refreshToken(jwtService.generateRefreshToken(username))
                .accessExpirationTime(accessExpiry.format(formatter))
                .refreshExpirationTime(refreshExpiry.format(formatter))
                .build();
    }
}
