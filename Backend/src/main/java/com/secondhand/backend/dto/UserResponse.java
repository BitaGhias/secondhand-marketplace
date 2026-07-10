package com.secondhand.backend.dto;

import com.secondhand.backend.constant.Role;

public class UserResponse {
    private Long id;
    private String fullName;
    private String username;
    private Role role;
    private boolean blocked;
    private String phoneNumber;
    private String email;

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
    public String getEmail() { return email; }  // ← اضافه شد

    public void setId(Long id) { this.id = id; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setUsername(String username) { this.username = username; }
    public void setRole(Role role) { this.role = role; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setEmail(String email) { this.email = email; }  // ← اضافه شد
}