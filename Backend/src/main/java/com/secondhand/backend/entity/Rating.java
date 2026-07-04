package com.secondhand.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ratings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public int score;

    public String comment;

    //  مربوط به کدام آگهی است؟
    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    public Item item;

    // کاربری که امتیاز را ثبت کرده (خریدار)
    @ManyToOne
    @JoinColumn(name = "rater_id", nullable = false)
    public User rater;

    // فروشنده‌ای که امتیاز را دریافت کرده است
    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    public User seller;
}
