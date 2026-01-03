package com.example.monoauction.service;


import com.example.monoauction.execptions.AuctionHouseException;
import com.example.monoauction.model.entity.AppUsers;
import com.example.monoauction.model.enums.ErrorMessage;
import com.example.monoauction.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;


    @Override
    @NonNull
    public AppUsers loadUserByUsername(@NonNull String userNameOrEmail) throws UsernameNotFoundException {
        return userRepository.findByEmail(userNameOrEmail).orElseThrow(() -> new AuctionHouseException(ErrorMessage.USER_NOT_FOUND));
    }

}

