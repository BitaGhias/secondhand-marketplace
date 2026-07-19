package com.secondhand.backend.entity;

import com.secondhand.backend.constant.ItemStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Double price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus status = ItemStatus.PENDING;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    // خریدار آگهی (پس از خرید ثبت می‌شود)
    @ManyToOne
    @JoinColumn(name = "buyer_id")
    private User buyer;

    public Item() {}

    public Item(Long id, String title, String description, Double price, ItemStatus status,
                LocalDateTime createdAt, User user, Category category, City city, String rejectionReason) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.status = status;
        this.createdAt = createdAt;
        this.user = user;
        this.category = category;
        this.city = city;
        this.rejectionReason = rejectionReason;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Double getPrice() { return price; }
    public ItemStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public User getUser() { return user; }
    public Category getCategory() { return category; }
    public City getCity() { return city; }
    public String getRejectionReason() { return rejectionReason; }
    public User getBuyer() { return buyer; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(Double price) { this.price = price; }
    public void setStatus(ItemStatus status) { this.status = status; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUser(User user) { this.user = user; }
    public void setCategory(Category category) { this.category = category; }
    public void setCity(City city) { this.city = city; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public void setBuyer(User buyer) { this.buyer = buyer; }
}