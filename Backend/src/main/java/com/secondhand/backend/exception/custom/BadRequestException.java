package com.secondhand.backend.exception.custom;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
//BadRequest
//400
//داده ارسالی نامعتبر است