package com.example.monoauction.model.dto;

import com.example.monoauction.model.enums.UserRole;
import lombok.Data;

@Data
public class RegisterDto {
    private String email;
    private String password;
    private String displayName;
    private UserRole role;
}
