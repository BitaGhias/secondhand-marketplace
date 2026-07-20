package com.secondhand.backend.dto.user;

public class LoginResponse {
    private UserResponse user;
    private String token;

    public LoginResponse() {}
    public LoginResponse(UserResponse user, String token) {
        this.user = user;
        this.token = token;
    }

    public UserResponse getUser() { return user; }
    public String getToken() { return token; }
    public void setUser(UserResponse user) { this.user = user; }
    public void setToken(String token) { this.token = token; }
}