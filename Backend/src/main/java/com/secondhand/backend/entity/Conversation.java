package com.secondhand.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "conversations", indexes = {
        @Index(name = "idx_conversations_item", columnList = "item_id"),
        @Index(name = "idx_conversations_buyer", columnList = "buyer_id"),
        @Index(name = "idx_conversations_seller", columnList = "seller_id")
})
/**
 * JPA entity representing a "conversation" record in the database.
 * <p>
 * This class defines the structure of the matching table in the SQLite database and is managed by Hibernate; relations between tables are declared with JPA annotations.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    /**
     * Creates a new {@code Conversation} instance.
     */
    public Conversation() {}

    public Conversation(Long id, Item item, User buyer, User seller) {
        this.id = id;
        this.item = item;
        this.buyer = buyer;
        this.seller = seller;
    }

    public Long getId() { return id; }
    public Item getItem() { return item; }
    public User getBuyer() { return buyer; }
    public User getSeller() { return seller; }

    public void setId(Long id) { this.id = id; }
    public void setItem(Item item) { this.item = item; }
    public void setBuyer(User buyer) { this.buyer = buyer; }
    public void setSeller(User seller) { this.seller = seller; }
}