package com.secondhand.backend.exception.custom;

/**
 * Error-handling type: "bad request exception".
 * <p>
 * This class is part of the centralized error-handling mechanism, ensuring that every error response has the same structure and a clear message.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class BadRequestException extends RuntimeException {
    /**
     * Creates a new {@code BadRequestException} instance.
     *
     * @param message the message text
     */
    public BadRequestException(String message) {
        super(message);
    }
}
//BadRequest
//400
//داده ارسالی نامعتبر است