package com.example.monoauction.user.controller;


import com.example.monoauction.common.dto.ApiResponse;
import com.example.monoauction.security.JwtTokenProvider;
import com.example.monoauction.user.dto.LoginRequest;
import com.example.monoauction.user.dto.RegisterRequest;
import com.example.monoauction.user.dto.Token;
import com.example.monoauction.user.dto.UserResponse;
import com.example.monoauction.user.model.User;
import com.example.monoauction.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(
            @Valid @RequestBody RegisterRequest request
    ){
        User user = authService.register(
                request.getEmail(),
                request.getPassword(),
                request.getFullName(),
                request.getRole()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("user", new UserResponse(user));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User Registered Successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        Token token = authService.login(request.getEmail(), request.getPassword());

        User user = authService.getUserByEmail(request.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("user", new UserResponse(user));
        response.put("token", token);

        return ResponseEntity.ok(ApiResponse.success("Login Successfull", response));

    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<Map<String, Object>>> refreshToken(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);

        Token newToken = authService.refreshToken(token);
        Map<String, Object> response = new HashMap<>();
        response.put("newToken",newToken);

        return ResponseEntity.ok(ApiResponse.success("New Token Generated Successfully", response));

    }

    //----------------------------------------------------------For Test-----------------------------------------------------------------------------------

    //----------------------------------------------------------For Extract Token-----------------------------------------------------------------------------------
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Invalid token");
    }


}



