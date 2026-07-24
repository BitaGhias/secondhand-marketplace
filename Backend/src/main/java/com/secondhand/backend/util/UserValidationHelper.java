package com.secondhand.backend.util;

import com.secondhand.backend.entity.User;
import com.secondhand.backend.exception.custom.ForbiddenException;

/**
 * Utility class providing "user validation helper" helpers.
 * <p>
 * This class is a helper utility whose methods are used across different parts of the application.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class UserValidationHelper {

    /**
     * Validates active and not blocked.
     *
     * @param user the user object
     */
    public static void validateActiveAndNotBlocked(User user) {
        if (!user.isActive()) {
            throw new ForbiddenException("حساب کاربری شما فعال نیست!");
        }
        if (user.isBlocked()) {
            throw new ForbiddenException("حساب کاربری شما مسدود شده است!");
        }
    }
}