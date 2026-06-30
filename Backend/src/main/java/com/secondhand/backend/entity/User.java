package com.secondhand.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data                // ۲. تولید خودکار Getter، Setter، toString، equals و hashCode
@NoArgsConstructor   // ۳. تولید سازنده بدون آرگومان
@AllArgsConstructor  // ۴. تولید سازنده با تمام آرگومان‌ها
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Column(nullable = false)
    private String email;

    private String role = "ROLE_USER";
    private boolean active = true;
}