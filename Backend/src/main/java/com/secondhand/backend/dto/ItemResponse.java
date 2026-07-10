package com.secondhand.backend.dto;

import java.util.List;

public class ItemResponse {
    private Long id;
    private String title;
    private String description;
    private double price;
    private String status;
    private String categoryName;
    private String parentCategoryName;
    private String cityName;
    private String ownerUsername;
    private Long ownerId;
    private List<ImageResponse> images;

    public ItemResponse() {}

    public ItemResponse(Long id, String title, String description, double price, String status,
                        String categoryName, String parentCategoryName, String cityName,
                        String ownerUsername, Long ownerId, List<ImageResponse> images) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.status = status;
        this.categoryName = categoryName;
        this.parentCategoryName = parentCategoryName;
        this.cityName = cityName;
        this.ownerUsername = ownerUsername;
        this.ownerId = ownerId;
        this.images = images;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getStatus() { return status; }
    public String getCategoryName() { return categoryName; }
    public String getParentCategoryName() { return parentCategoryName; }
    public String getCityName() { return cityName; }
    public String getOwnerUsername() { return ownerUsername; }
    public Long getOwnerId() { return ownerId; }
    public List<ImageResponse> getImages() { return images; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }
    public void setStatus(String status) { this.status = status; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public void setParentCategoryName(String parentCategoryName) { this.parentCategoryName = parentCategoryName; }
    public void setCityName(String cityName) { this.cityName = cityName; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public void setImages(List<ImageResponse> images) { this.images = images; }
}