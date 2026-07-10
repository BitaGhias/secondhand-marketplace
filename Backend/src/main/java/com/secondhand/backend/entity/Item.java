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

    private String status = "PENDING"; //  PENDING ، APPROVED ، REJECTED

    private LocalDateTime createdAt = LocalDateTime.now();

    // ارتباط آگهی با کاربر (هر آگهی متعلق به یک کاربر است)
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    public Category category;

    @ManyToOne
    @JoinColumn(name = "city_id", nullable = false)
    public City city;
}
