package com.secondhand.backend.exception.custom;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
// 404
// Not Found
// داده مورد نظر پیدا نشده است