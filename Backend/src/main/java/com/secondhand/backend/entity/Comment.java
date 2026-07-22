package com.secondhand.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String text;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // FIX (مورد ۴): نشانگر ویرایش‌شدن کامنت - مشابه فیلد isEdited در ChatMessage
    @Column(name = "is_edited", nullable = false)
    private boolean edited = false;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Comment() {
        this.createdAt = LocalDateTime.now();
    }

    public Comment(Long id, String text, LocalDateTime createdAt, Item item, User user) {
        this.id = id;
        this.text = text;
        this.createdAt = createdAt;
        this.item = item;
        this.user = user;
    }

    public Long getId() { return id; }
    public String getText() { return text; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isEdited() { return edited; }
    public Item getItem() { return item; }
    public User getUser() { return user; }

    public void setId(Long id) { this.id = id; }
    public void setText(String text) { this.text = text; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setEdited(boolean edited) { this.edited = edited; }
    public void setItem(Item item) { this.item = item; }
    public void setUser(User user) { this.user = user; }
}