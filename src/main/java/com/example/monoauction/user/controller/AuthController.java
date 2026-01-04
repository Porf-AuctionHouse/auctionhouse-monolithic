package com.example.monoauction.user.controller;


import com.example.monoauction.user.dto.LoginRequest;
import com.example.monoauction.user.dto.RegisterRequest;
import com.example.monoauction.user.dto.Token;
import com.example.monoauction.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private final AuthService authService;

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello");
    }

    @PostMapping("/login")
    public ResponseEntity<Token> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<Token> refreshToken(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        return ResponseEntity.ok(authService.refreshToken(token));

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



