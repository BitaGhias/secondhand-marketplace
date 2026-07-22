package com.secondhand.backend.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class UserUpdateRequest {
    @Size(max = 50, message = "نام کامل نباید بیشتر از ۵۰ کاراکتر باشد")
    private String fullName;

    private String phoneNumber;

    @Email(message = "فرمت ایمیل نامعتبر است")
    private String email;

    public UserUpdateRequest() {}

    public UserUpdateRequest(String fullName, String phoneNumber, String email) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }

    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setEmail(String email) { this.email = email; }
}