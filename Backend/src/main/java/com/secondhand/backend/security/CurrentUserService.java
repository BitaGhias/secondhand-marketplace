package com.secondhand.backend.security;

import com.secondhand.backend.exception.custom.UnauthorizedException;
import com.secondhand.backend.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserService userService;

    public CurrentUserService(UserService userService) {
        this.userService = userService;
    }

    public Long getCurrentUserId() {
        String username = getCurrentUsername();
        return userService.getUserIdByUsername(username);
    }

    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedException("کاربر وارد سیستم نشده است.");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }

        if (principal instanceof String username && !username.equals("anonymousUser")) {
            return username;
        }

        throw new UnauthorizedException("کاربر وارد سیستم نشده است.");
    }
}