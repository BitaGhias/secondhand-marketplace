package com.secondhand.backend.exception;

import java.time.LocalDateTime;

/**
 * Error-handling type: "error response".
 * <p>
 * This class is part of the centralized error-handling mechanism, ensuring that every error response has the same structure and a clear message.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class ErrorResponse {
    private String message; // خطا به فارسی
    private int statusCode;
    private String status; // نام وضعیت به انگلیسی
    private LocalDateTime timestamp;
    private String path;

    /**
     * Creates a new {@code ErrorResponse} instance.
     */
    public ErrorResponse() {}

    public ErrorResponse(String message, int statusCode, String status, String path) {
        this.message = message;
        this.statusCode = statusCode;
        this.status = status;
        this.timestamp = LocalDateTime.now();
        this.path = path;
    }

    public String getMessage() { return message; }
    public int getStatusCode() { return statusCode; }
    public String getStatus() { return status; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getPath() { return path; }

    public void setMessage(String message) { this.message = message; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }
    public void setStatus(String status) { this.status = status; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public void setPath(String path) { this.path = path; }
}