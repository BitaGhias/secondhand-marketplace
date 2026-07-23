package com.secondhand.backend.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object carrying "user update request" data between client and server.
 * <p>
 * This class is used purely for transferring data between client and server and is not mapped to the database directly, keeping the internal structure of the entities hidden from the client.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class UserUpdateRequest {
    @Size(max = 50, message = "نام کامل نباید بیشتر از ۵۰ کاراکتر باشد")
    private String fullName;

    private String phoneNumber;

    @Email(message = "فرمت ایمیل نامعتبر است")
    private String email;

    /**
     * Creates a new {@code UserUpdateRequest} instance.
     */
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