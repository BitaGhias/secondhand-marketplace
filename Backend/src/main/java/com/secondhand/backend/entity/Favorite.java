package com.secondhand.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "favorites",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "item_id"})
        }
)
/**
 * JPA entity representing a "favorite" record in the database.
 * <p>
 * This class defines the structure of the matching table in the SQLite database and is managed by Hibernate; relations between tables are declared with JPA annotations.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
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

    /**
     * Creates a new {@code Favorite} instance.
     */
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