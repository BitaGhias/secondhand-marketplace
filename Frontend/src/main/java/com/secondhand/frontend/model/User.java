package com.secondhand.frontend.model;

public class User {
    private Long id;
    private String fullName;
    private String username;
    private String role;
    private boolean blocked;
    private String phoneNumber;
    private String email;

    public User() {}

    public User(Long id, String fullName, String username, String role,
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
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}