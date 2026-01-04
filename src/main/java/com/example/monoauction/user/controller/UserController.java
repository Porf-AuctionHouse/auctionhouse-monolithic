package com.example.monoauction.user.controller;


import com.example.monoauction.common.execptions.AuctionHouseException;
import com.example.monoauction.user.model.User;
import com.example.monoauction.common.enums.ErrorMessage;
import com.example.monoauction.user.dto.UserResponse;
import com.example.monoauction.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    @Autowired
    private final UserRepository userRepository;


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();

        return users.stream().map(user -> UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole().toString())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build())
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new AuctionHouseException(ErrorMessage.USER_NOT_FOUND));
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole().toString())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    @GetMapping("/email/{email}")
    public UserResponse getUserByEmail(@PathVariable String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AuctionHouseException(ErrorMessage.USER_NOT_FOUND));
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole().toString())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
