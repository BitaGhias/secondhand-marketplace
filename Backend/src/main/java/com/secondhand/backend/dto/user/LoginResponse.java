package com.secondhand.backend.dto.user;

/**
 * Data Transfer Object carrying "login response" data between client and server.
 * <p>
 * This class is used purely for transferring data between client and server and is not mapped to the database directly, keeping the internal structure of the entities hidden from the client.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class LoginResponse {
    private UserResponse user;
    private String token;

    /**
     * Creates a new {@code LoginResponse} instance.
     */
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