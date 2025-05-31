package com.sarthak.airbnb.util;

import com.sarthak.airbnb.entity.User;
import com.sarthak.airbnb.exceptions.UnAuthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AppUtils {

    public static  User getCurrentUser(){
//        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            return (User) auth.getPrincipal();
        }
        else {
            throw new UnAuthorizedException("User not authorized");
        }
    }
}
