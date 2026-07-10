package com.secondhand.backend.exception.custom;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
// 403
// Forbidden
// کاربر اجازه ی انجام این عملیات را ندارد