package com.secondhand.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String text;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Comment() {}

    public Comment(Long id, String text, Item item, User user) {
        this.id = id;
        this.text = text;
        this.item = item;
        this.user = user;
    }

    public Long getId() { return id; }
    public String getText() { return text; }
    public Item getItem() { return item; }
    public User getUser() { return user; }

    public void setId(Long id) { this.id = id; }
    public void setText(String text) { this.text = text; }
    public void setItem(Item item) { this.item = item; }
    public void setUser(User user) { this.user = user; }
}