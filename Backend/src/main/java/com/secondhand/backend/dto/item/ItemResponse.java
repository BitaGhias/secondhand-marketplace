package com.secondhand.backend.dto.item;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object carrying "item response" data between client and server.
 * <p>
 * This class is used purely for transferring data between client and server and is not mapped to the database directly, keeping the internal structure of the entities hidden from the client.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class ItemResponse {
    private Long id;
    private String title;
    private String description;
    private Long price;
    private String status;
    private String categoryName;
    private String parentCategoryName;
    private String cityName;
    private String ownerUsername;
    private Long ownerId;
    private List<ImageResponse> images;
    private String rejectionReason;
    private Long buyerId;
    private String buyerUsername;
    private Long categoryId;
    private Long cityId;
    private Double averageRating;
    // تاریخ ثبت آگهی
    private LocalDateTime createdAt;

    /**
     * Creates a new {@code ItemResponse} instance.
     */
    public ItemResponse() {}

    public ItemResponse(Long id, String title, String description, Long price, String status,
                        String categoryName, String parentCategoryName, String cityName,
                        String ownerUsername, Long ownerId, List<ImageResponse> images,
                        String rejectionReason) {
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
        this.rejectionReason = rejectionReason;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Long getPrice() { return price; }
    public String getStatus() { return status; }
    public String getCategoryName() { return categoryName; }
    public String getParentCategoryName() { return parentCategoryName; }
    public String getCityName() { return cityName; }
    public String getOwnerUsername() { return ownerUsername; }
    public Long getOwnerId() { return ownerId; }
    public List<ImageResponse> getImages() { return images; }
    public String getRejectionReason() { return rejectionReason; }
    public Long getBuyerId() { return buyerId; }
    public String getBuyerUsername() { return buyerUsername; }
    public Long getCategoryId() { return categoryId; }
    public Long getCityId() { return cityId; }
    public Double getAverageRating() { return averageRating; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(Long price) { this.price = price; }
    public void setStatus(String status) { this.status = status; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public void setParentCategoryName(String parentCategoryName) { this.parentCategoryName = parentCategoryName; }
    public void setCityName(String cityName) { this.cityName = cityName; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public void setImages(List<ImageResponse> images) { this.images = images; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }
    public void setBuyerUsername(String buyerUsername) { this.buyerUsername = buyerUsername; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public void setCityId(Long cityId) { this.cityId = cityId; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
