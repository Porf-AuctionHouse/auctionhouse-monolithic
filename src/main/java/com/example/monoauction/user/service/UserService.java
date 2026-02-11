package com.example.monoauction.user.service;

import com.example.monoauction.common.enums.UserRole;
import com.example.monoauction.common.execptions.ResourceNotFoundException;
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

    public User registerUser(String email, String password, String fullName, UserRole role) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email Already In Use Plz Try Different Email");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setRole(role);
        user.setIsVerified(false);
        user.setIsActive(true);
        user.setBalance(BigDecimal.ZERO);

        return userRepository.save(user);
    }

    public User getUserById(Long id){
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User Not Found With These Details"));
    }

    public User getUserByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found With These Details"));
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
            throw new RuntimeException("Insufficient Balance");
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

    public User saveUser(User user){
        return userRepository.save(user);
    }
}
