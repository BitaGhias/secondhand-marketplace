package com.secondhand.backend.entity;

import jakarta.persistence.*;

/**
 * JPA entity representing a "image" record in the database.
 * <p>
 * This class defines the structure of the matching table in the SQLite database and is managed by Hibernate; relations between tables are declared with JPA annotations.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
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