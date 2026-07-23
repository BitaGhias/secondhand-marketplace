package com.secondhand.backend.dto.user;

import com.secondhand.backend.constant.Role;

/**
 * Data Transfer Object carrying "user response" data between client and server.
 * <p>
 * This class is used purely for transferring data between client and server and is not mapped to the database directly, keeping the internal structure of the entities hidden from the client.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class UserResponse {
    private Long id;
    private String fullName;
    private String username;
    private Role role;
    private boolean blocked;
    private String phoneNumber;
    private String email;
    private String profileImagePath;

    /**
     * Creates a new {@code UserResponse} instance.
     */
    public UserResponse() {}

    public UserResponse(Long id, String fullName, String username, Role role,
                        boolean blocked, String phoneNumber, String email) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.role = role;
        this.blocked = blocked;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getUsername() { return username; }
    public Role getRole() { return role; }
    public boolean isBlocked() { return blocked; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
    public String getProfileImagePath() { return profileImagePath; }

    public void setId(Long id) { this.id = id; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setUsername(String username) { this.username = username; }
    public void setRole(Role role) { this.role = role; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setEmail(String email) { this.email = email; }
    public void setProfileImagePath(String profileImagePath) { this.profileImagePath = profileImagePath; }
}