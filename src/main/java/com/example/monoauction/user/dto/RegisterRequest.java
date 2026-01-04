package com.example.monoauction.user.dto;

import com.example.monoauction.common.enums.UserRole;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String fullName;
    private String phoneNumber;
    private UserRole role;
}
