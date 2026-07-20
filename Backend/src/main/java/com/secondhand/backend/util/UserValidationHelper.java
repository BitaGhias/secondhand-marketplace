package com.secondhand.backend.util;

import com.secondhand.backend.entity.User;
import com.secondhand.backend.exception.custom.ForbiddenException;

/**
 * کلاس کمکی برای اعتبارسنجی وضعیت کاربر
 */
public class UserValidationHelper {

    public static void validateActiveAndNotBlocked(User user) {
        if (!user.isActive()) {
            throw new ForbiddenException("حساب کاربری شما فعال نیست!");
        }
        if (user.isBlocked()) {
            throw new ForbiddenException("حساب کاربری شما مسدود شده است!");
        }
    }
}