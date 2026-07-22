package com.secondhand.backend.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserRegisterRequest {
    @NotBlank(message = "نام کامل الزامی است")
    @Size(max = 50, message = "نام کامل نباید بیشتر از ۵۰ کاراکتر باشد")
    private String fullName;

    @NotBlank(message = "نام کاربری الزامی است")
    @Size(min = 3, max = 20, message = "نام کاربری باید بین ۳ تا ۲۰ کاراکتر باشد")
    private String username;

    @NotBlank(message = "رمز عبور الزامی است")
    @Size(min = 6, max = 100, message = "رمز عبور باید بین ۶ تا ۱۰۰ کاراکتر باشد")
    private String password;

    @NotBlank(message = "تکرار رمز عبور الزامی است")
    private String confirmPassword;

    @NotBlank(message = "شماره تلفن الزامی است")
    private String phoneNumber;

    @NotBlank(message = "ایمیل الزامی است")
    @Email(message = "فرمت ایمیل نامعتبر است")
    private String email;

    public UserRegisterRequest() {}

    public UserRegisterRequest(String fullName, String username, String password, String confirmPassword,
                               String phoneNumber, String email) {
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    public String getFullName() { return fullName; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getConfirmPassword() { return confirmPassword; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }

    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setEmail(String email) { this.email = email; }
}