package com.example.monoauction.authentication.model.dto;

import com.example.monoauction.authentication.model.enums.UserRole;
import lombok.Data;

@Data
public class RegisterDto {
    private String email;
    private String password;
    private String displayName;
    private UserRole role;
}
