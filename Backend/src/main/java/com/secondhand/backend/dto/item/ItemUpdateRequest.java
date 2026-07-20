package com.secondhand.backend.dto.item;

public class ItemUpdateRequest {
    private String title;
    private String description;
    private Long price;
    private Long categoryId;
    private Long cityId;
    private String rejectionReason;

    public ItemUpdateRequest() {}

    public ItemUpdateRequest(String title, String description, Long price,
                             Long categoryId, Long cityId, String rejectionReason) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.categoryId = categoryId;
        this.cityId = cityId;
        this.rejectionReason = rejectionReason;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Long getPrice() { return price; }
    public Long getCategoryId() { return categoryId; }
    public Long getCityId() { return cityId; }
    public String getRejectionReason() { return rejectionReason; }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(Long price) { this.price = price; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public void setCityId(Long cityId) { this.cityId = cityId; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}