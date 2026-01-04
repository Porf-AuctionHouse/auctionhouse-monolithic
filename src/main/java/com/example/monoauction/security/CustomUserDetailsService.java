package com.example.monoauction.security;


import com.example.monoauction.common.execptions.AuctionHouseException;
import com.example.monoauction.user.model.User;
import com.example.monoauction.common.enums.ErrorMessage;
import com.example.monoauction.user.repository.UserRepository;
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
    public User loadUserByUsername(@NonNull String userNameOrEmail) throws UsernameNotFoundException {
        return userRepository.findByEmail(userNameOrEmail).orElseThrow(() -> new AuctionHouseException(ErrorMessage.USER_NOT_FOUND));
    }

}

