package com.secondhand.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "conversations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    public Item item;

    //خریدار
    @ManyToOne
    @JoinColumn(name = "buyer_id", nullable = false)
    public User buyer;

    // فروشنده‌ای که صاحب آگهی است
    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    public User seller;
}
