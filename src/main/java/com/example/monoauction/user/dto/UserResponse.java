package com.example.monoauction.user.dto;

import com.example.monoauction.common.enums.UserRole;
import com.example.monoauction.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private UserRole role;
    private Boolean isVerified;
    private Boolean isActive;
    private BigDecimal balance;
    private LocalDateTime createdAt;

    public UserResponse(User user){
        this.id = user.getId();
        this.email = user.getEmail();
        this.fullName = user.getFullName();
        this.phoneNumber = user.getPhoneNumber();
        this.role = user.getRole();
        this.isVerified = user.getIsVerified();
        this.isActive = user.getIsActive();
        this.balance = user.getBalance();
        this.createdAt = user.getCreatedAt();
    }
}
