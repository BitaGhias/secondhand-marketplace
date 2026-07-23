package com.secondhand.backend.entity;

import com.secondhand.backend.constant.Role;
import jakarta.persistence.*;

/**
 * JPA entity representing a "user" record in the database.
 * <p>
 * This class defines the structure of the matching table in the SQLite database and is managed by Hibernate; relations between tables are declared with JPA annotations.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
@Entity //برای ساخت جدول توی DB
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //برای تولید خودکار مقدار فیلد
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password; // پسورد هش شده

    @Column(name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING) // بخاطر اینکه به صورت رشته در DB ذحیره شه
    @Column(nullable = false)
    private Role role = Role.USER;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean blocked = false;

    // مسیر عکس پروفایل
    @Column(name = "profile_image_path")
    private String profileImagePath;

    /**
     * Creates a new {@code User} instance.
     */
    public User() {}

    public User(Long id, String fullName, String username, String password,
                String phoneNumber, String email, Role role, boolean active, boolean blocked) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.role = role;
        this.active = active;
        this.blocked = blocked;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
    public boolean isActive() { return active; }
    public boolean isBlocked() { return blocked; }
    public String getProfileImagePath() { return profileImagePath; }

    public void setId(Long id) { this.id = id; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(Role role) { this.role = role; }
    public void setActive(boolean active) { this.active = active; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
    public void setProfileImagePath(String profileImagePath) { this.profileImagePath = profileImagePath; }
}