package com.secondhand.backend.exception.custom;

/**
 * Error-handling type: "unauthorized exception".
 * <p>
 * This class is part of the centralized error-handling mechanism, ensuring that every error response has the same structure and a clear message.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class UnauthorizedException extends RuntimeException {
    /**
     * Creates a new {@code UnauthorizedException} instance.
     *
     * @param message the message text
     */
    public UnauthorizedException(String message) {
        super(message);
    }
}
// 401
// Unauthorized
// کاربر وارد سیستم نشده است