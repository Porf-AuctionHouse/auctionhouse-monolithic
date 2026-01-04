package com.example.monoauction.user.service;

import com.example.monoauction.common.enums.ErrorMessage;
import com.example.monoauction.common.enums.UserRole;
import com.example.monoauction.common.execptions.AuctionHouseException;
import com.example.monoauction.user.dto.RegisterRequest;
import com.example.monoauction.user.dto.UserResponse;
import com.example.monoauction.user.model.User;
import com.example.monoauction.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public User registerUser(RegisterRequest request){
        if(userRepository.existsByEmail(request.getEmail())){
            throw new AuctionHouseException(ErrorMessage.EMAIL_ALREADY_EXISTS);
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setRole(request.getRole());
        user.setIsVerified(false);
        user.setIsActive(true);
        user.setBalance(BigDecimal.ZERO);
        return userRepository.save(user);

    }

    public User getUserById(Long id){
        return userRepository.findById(id)
                .orElseThrow(() -> new AuctionHouseException(ErrorMessage.USER_NOT_FOUND));
    }

    public User getUserByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AuctionHouseException(ErrorMessage.USER_NOT_FOUND));
    }

    public User updateProfile(Long userId, String fullName, String phoneNumber){
        User user = getUserById(userId);
        user.setFullName(fullName);
        user.setPhoneNumber(phoneNumber);
        return userRepository.save(user);
    }

    public User updatePassword(Long userId, String password){
        User user = getUserById(userId);
        user.setPassword(passwordEncoder.encode(password));
        return userRepository.save(user);
    }

    public User addBalance(Long userId, BigDecimal amount){
        User user = getUserById(userId);
        user.setBalance(user.getBalance().add(amount));
        return userRepository.save(user);
    }

    public User deductBalance(Long userId, BigDecimal amount){
        User user = getUserById(userId);
        if(user.getBalance().compareTo(amount) < 0){
            throw new AuctionHouseException(ErrorMessage.INSUFFICIENT_BALANCE);
        }
        user.setBalance(user.getBalance().subtract(amount));
        return userRepository.save(user);
    }

    public User verifyUser(Long userId){
        User user = getUserById(userId);
        user.setIsVerified(true);
        return userRepository.save(user);
    }

    public User deactivateUser(Long userId){
        User user = getUserById(userId);
        user.setIsActive(false);
        return userRepository.save(user);
    }

    public List<User> getAllSellers(){
        return userRepository.findByRole(UserRole.SELLER);
    }

    public List<User> getAllActiveUsers(){
        return userRepository.findByIsActiveTrue();
    }
}
