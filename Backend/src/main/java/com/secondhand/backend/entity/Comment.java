package com.secondhand.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public String text;

    // متصل به آگهی مورد نظر
    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    public Item item;

    // کاربری که کامنت را گذاشته است
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    public User user;
}