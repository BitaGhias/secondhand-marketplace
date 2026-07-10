package com.secondhand.backend.exception;

import java.time.LocalDateTime;

public class ErrorResponse {
    private String message;
    private int statusCode;
    private String status;
    private LocalDateTime timestamp;
    private String path;

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