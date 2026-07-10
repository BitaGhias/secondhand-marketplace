package com.secondhand.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "conversations")
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