package com.secondhand.backend.dto.user;

public class UserRegisterRequest {
    private String fullName;
    private String username;
    private String password;
    private String phoneNumber;
    private String email;

    public UserRegisterRequest() {}

    public UserRegisterRequest(String fullName, String username, String password,
                               String phoneNumber, String email) {
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    public String getFullName() { return fullName; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }

    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setEmail(String email) { this.email = email; }
}