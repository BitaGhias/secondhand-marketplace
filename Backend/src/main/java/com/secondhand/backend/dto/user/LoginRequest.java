package com.secondhand.backend.dto.user;

import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object carrying "login request" data between client and server.
 * <p>
 * This class is used purely for transferring data between client and server and is not mapped to the database directly, keeping the internal structure of the entities hidden from the client.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class LoginRequest {
    @NotBlank(message = "نام کاربری الزامی است")
    private String username;

    @NotBlank(message = "رمز عبور الزامی است")
    private String password;

    /**
     * Creates a new {@code LoginRequest} instance.
     */
    public LoginRequest() {}
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
}