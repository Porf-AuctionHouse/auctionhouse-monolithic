package com.example.monoauction.user.controller;


import com.example.monoauction.common.dto.ApiResponse;
import com.example.monoauction.user.dto.AddBalanceRequest;
import com.example.monoauction.user.dto.UpdateProfileRequest;
import com.example.monoauction.user.model.User;
import com.example.monoauction.user.dto.UserResponse;
import com.example.monoauction.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

//    @PostMapping("/register")
//    public ResponseEntity<ApiResponse<UserResponse>> registerUser(
//            @Valid @RequestBody RegisterRequest request){
//
//        User user = userService.registerUser(
//                request.getEmail(),
//                request.getPassword(),
//                request.getFullName(),
//                request.getRole()
//        );
//
//        return ResponseEntity.status(HttpStatus.CREATED).body(
//                ApiResponse.success("User registered successfully", new UserResponse(user))
//        );
//
//    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id){
        User user = userService.getUserById(id);

        return ResponseEntity.ok(ApiResponse.success(new UserResponse(user)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProfileRequest request){
        User user = userService.updateProfile(id, request.getFullName(), request.getPhoneNumber());
        return ResponseEntity.ok(ApiResponse.success(
                "Profile updated successfully",new UserResponse(user)));
    }

    @PostMapping("/{id}/balance")
    public ResponseEntity<ApiResponse<UserResponse>> addBalance(
            @PathVariable Long id,
            @Valid @RequestBody AddBalanceRequest request) {
        User user = userService.addBalance(id, request.getAmount());
        return ResponseEntity.ok(ApiResponse.success(
                "Balance added successfully", new UserResponse(user)));
    }
}
