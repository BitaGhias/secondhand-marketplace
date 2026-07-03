package com.secondhand.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Double price;

    private String status = "PENDING"; // وضعیت آگهی: PENDING (منتظر تایید)، APPROVED (تایید شده)، REJECTED (رد شده)

    private LocalDateTime createdAt = LocalDateTime.now();

    // ارتباط آگهی با کاربر (هر آگهی متعلق به یک کاربر است)
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
