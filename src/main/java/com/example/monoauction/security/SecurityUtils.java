package com.example.monoauction.security;

import com.example.monoauction.user.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {
    public static User getCurrentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication != null && authentication.getPrincipal() instanceof User){
            return (User) authentication.getPrincipal();
        }

        throw new RuntimeException("User Not Authenticated");

    }

    public static Long getCurrentUserId(){
        return (Long) getCurrentUser().getId();
    }
}
