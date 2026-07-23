package com.secondhand.backend.dto.item;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * Data Transfer Object carrying "item update request" data between client and server.
 * <p>
 * This class is used purely for transferring data between client and server and is not mapped to the database directly, keeping the internal structure of the entities hidden from the client.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class ItemUpdateRequest {
    private String title;
    private String description;
    private Long price;
    private Long categoryId;
    private Long cityId;
    private String rejectionReason;
    // FIX: پشتیبانی از تغییر تصاویر هنگام ویرایش آگهی
    private List<Long> removedImageIds;
    private List<MultipartFile> images;

    /**
     * Creates a new {@code ItemUpdateRequest} instance.
     */
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
    public List<Long> getRemovedImageIds() { return removedImageIds; }
    public List<MultipartFile> getImages() { return images; }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(Long price) { this.price = price; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public void setCityId(Long cityId) { this.cityId = cityId; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public void setRemovedImageIds(List<Long> removedImageIds) { this.removedImageIds = removedImageIds; }
    public void setImages(List<MultipartFile> images) { this.images = images; }
}