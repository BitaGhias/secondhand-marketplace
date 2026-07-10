package com.secondhand.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "images")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imagePath;  // مسیر فایل تصویر

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;  // این تصویر متعلق به کدام آگهی است

    public Image() {}

    public Image(Long id, String imagePath, Item item) {
        this.id = id;
        this.imagePath = imagePath;
        this.item = item;
    }

    public Long getId() { return id; }
    public String getImagePath() { return imagePath; }
    public Item getItem() { return item; }

    public void setId(Long id) { this.id = id; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public void setItem(Item item) { this.item = item; }
}