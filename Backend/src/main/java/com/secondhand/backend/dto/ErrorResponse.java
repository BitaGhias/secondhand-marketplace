package com.secondhand.backend.dto;

public class ErrorResponse {
    private String message;
    private int statusCode;

    public ErrorResponse() {}
    public ErrorResponse(String message, int statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }

    public String getMessage() { return message; }
    public int getStatusCode() { return statusCode; }
    public void setMessage(String message) { this.message = message; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }
}