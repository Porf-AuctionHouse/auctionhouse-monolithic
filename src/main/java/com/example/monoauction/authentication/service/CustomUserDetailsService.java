package com.example.monoauction.authentication.service;


import com.example.monoauction.common.execptions.AuctionHouseException;
import com.example.monoauction.authentication.model.entity.AppUsers;
import com.example.monoauction.authentication.model.enums.ErrorMessage;
import com.example.monoauction.authentication.repository.UserRepository;
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

