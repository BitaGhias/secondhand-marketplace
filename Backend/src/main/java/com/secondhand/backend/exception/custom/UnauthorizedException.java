package com.secondhand.backend.exception.custom;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
// 401
// Unauthorized
// کاربر وارد سیستم نشده است