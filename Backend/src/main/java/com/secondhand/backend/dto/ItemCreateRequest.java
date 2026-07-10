package com.secondhand.backend.dto;

public class ItemCreateRequest {
    private String title;
    private String description;
    private double price;
    private Long categoryId;
    private Long cityId;

    public ItemCreateRequest() {}
    public ItemCreateRequest(String title, String description, double price, Long categoryId, Long cityId) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.categoryId = categoryId;
        this.cityId = cityId;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public Long getCategoryId() { return categoryId; }
    public Long getCityId() { return cityId; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public void setCityId(Long cityId) { this.cityId = cityId; }
}