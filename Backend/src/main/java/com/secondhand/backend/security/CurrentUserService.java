package com.secondhand.backend.security;

import com.secondhand.backend.exception.custom.UnauthorizedException;
import com.secondhand.backend.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Configuration class: "current user service".
 * <p>
 * This class is part of the application security configuration and is loaded by Spring at startup.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
@Service
public class CurrentUserService { // خارج کردن هویت کاربر از توکن

    private final UserService userService;

    /**
     * Creates a new {@code CurrentUserService} instance.
     *
     * @param userService the "user service" value of type {@code UserService}
     */
    public CurrentUserService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Gets current user id.
     *
     * @return the resulting numeric value
     */
    public Long getCurrentUserId() {
        String username = getCurrentUsername();
        return userService.getUserIdByUsername(username);
    }

    /**
     * Gets current username.
     *
     * @return the resulting string
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedException("کاربر وارد سیستم نشده است.");
        }

        Object principal = authentication.getPrincipal(); // اطلاعات کاربر لاگین کرده

        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }

        if (principal instanceof String username && !username.equals("anonymousUser")) {
            return username;
        }

        throw new UnauthorizedException("کاربر وارد سیستم نشده است.");
    }
}