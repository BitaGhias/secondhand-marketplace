package com.secondhand.backend.exception.custom;

/**
 * Error-handling type: "resource not found exception".
 * <p>
 * This class is part of the centralized error-handling mechanism, ensuring that every error response has the same structure and a clear message.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class ResourceNotFoundException extends RuntimeException {
    /**
     * Creates a new {@code ResourceNotFoundException} instance.
     *
     * @param message the message text
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
// 404
// Not Found
// داده مورد نظر پیدا نشده است