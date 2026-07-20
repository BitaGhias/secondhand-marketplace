package com.secondhand.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "favorites",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "item_id"})
        }
)
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    public Favorite() {}

    public Favorite(Long id, User user, Item item) {
        this.id = id;
        this.user = user;
        this.item = item;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Item getItem() { return item; }

    public void setId(Long id) { this.id = id; }
    public void setUser(User user) { this.user = user; }
    public void setItem(Item item) { this.item = item; }
}