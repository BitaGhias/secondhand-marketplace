package com.secondhand.frontend.model;

/**
 * Client-side model representing "user" data returned by the server.
 * <p>
 * This class is the client-side representation of data received from the server and is deserialized from JSON by Jackson.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class User {
    private Long id;
    private String fullName;
    private String username;
    private String role;
    private boolean blocked;
    private String phoneNumber;
    private String email;
    private String profileImagePath;

    /**
     * Creates a new {@code User} instance.
     */
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
    /**
     * Sets blocked.
     *
     * @param blocked the "blocked" value of type {@code boolean}
     */
    public void setBlocked(boolean blocked) { this.blocked = blocked; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getProfileImagePath() { return profileImagePath; }
    public void setProfileImagePath(String profileImagePath) { this.profileImagePath = profileImagePath; }

    public String getProfileImageUrl() {
        if (profileImagePath == null || profileImagePath.isBlank()) return null;
        String normalized = profileImagePath.replace("\\", "/");
        if (normalized.startsWith("http")) return normalized;
        if (!normalized.startsWith("/")) normalized = "/" + normalized;
        return "http://127.0.0.1:8080" + normalized;
    }

    /**
     * Performs the "to string" operation.
     *
     * @return the resulting string
     */
    @Override
    public String toString() {
        return username != null ? username : "";
    }
}
