package com.example.monoauction.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomPrincipal {
    private String userId;
    private String email;
    private String role;
}
