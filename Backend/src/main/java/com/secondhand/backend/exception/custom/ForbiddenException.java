package com.secondhand.backend.exception.custom;

/**
 * Error-handling type: "forbidden exception".
 * <p>
 * This class is part of the centralized error-handling mechanism, ensuring that every error response has the same structure and a clear message.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class ForbiddenException extends RuntimeException {
    /**
     * Creates a new {@code ForbiddenException} instance.
     *
     * @param message the message text
     */
    public ForbiddenException(String message) {
        super(message);
    }
}
// 403
// Forbidden
// کاربر اجازه ی انجام این عملیات را ندارد