package com.secondhand.backend.dto;

public class ItemUpdateRequest {
    private String title;
    private String description;
    private Double price;
    private Long categoryId;
    private Long cityId;

    public ItemUpdateRequest() {}

    public ItemUpdateRequest(String title, String description, Double price, Long categoryId, Long cityId) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.categoryId = categoryId;
        this.cityId = cityId;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Double getPrice() { return price; }
    public Long getCategoryId() { return categoryId; }
    public Long getCityId() { return cityId; }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(Double price) { this.price = price; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public void setCityId(Long cityId) { this.cityId = cityId; }
}