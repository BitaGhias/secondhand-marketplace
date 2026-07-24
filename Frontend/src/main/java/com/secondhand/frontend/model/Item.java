package com.secondhand.frontend.model;

import java.util.List;

/**
 * Client-side model representing "item" data returned by the server.
 * <p>
 * This class is the client-side representation of data received from the server and is deserialized from JSON by Jackson.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class Item {

    public Long id;
    public String title;
    public String description;
    public Long price; // تغییر به Long برای پشتیبانی از مبالغ بزرگ
    public String status;
    public String categoryName;
    public String parentCategoryName;
    public String cityName;
    public String ownerUsername;
    public Long ownerId;
    public String ownerProfileImagePath;
    public List<Image> images;
    public String rejectionReason;
    public Double averageRating;
    public Long categoryId;
    public Long cityId;
    public Long buyerId;
    public String buyerUsername;

    // ===== Constructors =====
    /**
     * Creates a new {@code Item} instance.
     */
    public Item() {}

    public Item(Long id, String title, String description, Long price, String status,
                String categoryName, String parentCategoryName, String cityName,
                String ownerUsername, Long ownerId, List<Image> images,
                String rejectionReason, Double averageRating) {
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
        this.averageRating = averageRating;
    }

    // ===== Getters & Setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getParentCategoryName() { return parentCategoryName; }
    public void setParentCategoryName(String parentCategoryName) { this.parentCategoryName = parentCategoryName; }

    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }

    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getOwnerProfileImagePath() { return ownerProfileImagePath; }
    public void setOwnerProfileImagePath(String ownerProfileImagePath) { this.ownerProfileImagePath = ownerProfileImagePath; }

    /**
     * Builds the absolute URL of the owner's profile image.
     *
     * @return the absolute image URL, or {@code null} when the owner has no profile image
     */
    public String getOwnerProfileImageUrl() {
        if (ownerProfileImagePath == null || ownerProfileImagePath.isBlank()) return null;
        String normalized = ownerProfileImagePath.replace("\\", "/");
        if (normalized.startsWith("http")) return normalized;
        if (!normalized.startsWith("/")) normalized = "/" + normalized;
        return "http://127.0.0.1:8080" + normalized;
    }

    public List<Image> getImages() { return images; }
    public void setImages(List<Image> images) { this.images = images; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public Long getCategoryId() { return categoryId; }
    /**
     * Sets category id.
     *
     * @param categoryId id of the category
     */
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public Long getCityId() { return cityId; }
    public void setCityId(Long cityId) { this.cityId = cityId; }

    public Long getBuyerId() { return buyerId; }
    public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }

    public String getBuyerUsername() { return buyerUsername; }
    public void setBuyerUsername(String buyerUsername) { this.buyerUsername = buyerUsername; }

    public String getFormattedPrice() {
        if (price == null || price == 0) return "توافقی";
        return String.format("%,d تومان", price);
    }

    /**
     * Gets short price.
     *
     * @return the resulting string
     */
    public String getShortPrice() {
        if (price == null || price == 0) return "توافقی";

        // تقسیم بر double (مثلا 1000000.0) انجام شد تا خروجی اعشاری درست نمایش داده شود
        if (price >= 1_000_000_000) {
            return String.format("%.1f میلیارد تومان", price / 1_000_000_000.0);
        } else if (price >= 1_000_000) {
            return String.format("%.1f میلیون تومان", price / 1_000_000.0);
        } else if (price >= 1_000) {
            return String.format("%.1f هزار تومان", price / 1_000.0);
        } else {
            return String.format("%d تومان", price);
        }
    }

    /**
     * Gets persian status.
     *
     * @return the resulting string
     */
    public String getPersianStatus() {
        if (status == null) return "نامشخص";
        return switch (status.toUpperCase()) {
            case "PENDING" -> "در انتظار بررسی";
            case "ACTIVE", "APPROVED" -> "فعال";
            case "SOLD" -> "فروخته شده";
            case "DELETED" -> "حذف شده";
            case "REJECTED" -> "رد شده";
            default -> status;
        };
    }

    /**
     * Gets status color.
     *
     * @return the resulting string
     */
    public String getStatusColor() {
        if (status == null) return "#94a3b8";
        return switch (status.toUpperCase()) {
            case "PENDING" -> "#d97706";    // زرد
            case "APPROVED", "ACTIVE" -> "#16a34a";     // سبز
            case "SOLD" -> "#2563eb";       // آبی
            case "DELETED" -> "#dc2626";    // قرمز
            case "REJECTED" -> "#dc2626";   // قرمز
            default -> "#94a3b8";
        };
    }

    /**
     * Checks whether the "active" condition holds.
     *
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     */
    public boolean isActive() {
        return "APPROVED".equalsIgnoreCase(status) || "ACTIVE".equalsIgnoreCase(status);
    }

    /**
     * Checks whether the "pending" condition holds.
     *
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     */
    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(status);
    }

    /**
     * Checks whether the "sold" condition holds.
     *
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     */
    public boolean isSold() {
        return "SOLD".equalsIgnoreCase(status);
    }

    /**
     * Checks whether the "owner" condition holds.
     *
     * @param userId id of the user
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     */
    public boolean isOwner(Long userId) {
        return ownerId != null && ownerId.equals(userId);
    }

    /**
     * Gets full title.
     *
     * @return the resulting string
     */
    public String getFullTitle() {
        String category = categoryName != null ? categoryName : "";
        String parent = parentCategoryName != null ? parentCategoryName : "";
        if (!parent.isEmpty() && !category.isEmpty()) {
            return title + " (" + parent + " › " + category + ")";
        } else if (!category.isEmpty()) {
            return title + " (" + category + ")";
        }
        return title;
    }

    /**
     * Checks whether the "images" condition holds.
     *
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     */
    public boolean hasImages() {
        return images != null && !images.isEmpty();
    }

    /**
     * Gets image count.
     *
     * @return the resulting numeric value
     */
    public int getImageCount() {
        return images != null ? images.size() : 0;
    }

    /**
     * Gets first image url.
     *
     * @return the resulting string
     */
    public String getFirstImageUrl() {
        if (hasImages()) {
            return images.get(0).getFullUrl();
        }
        return null;
    }

    /**
     * Gets formatted rating.
     *
     * @return the resulting string
     */
    public String getFormattedRating() {
        if (averageRating == null) {
            return "بدون امتیاز";
        }
        return String.format("⭐ %.1f", averageRating);
    }

    /**
     * Gets rating with count.
     *
     * @param count the "count" value of type {@code int}
     * @return the resulting string
     */
    public String getRatingWithCount(int count) {
        if (averageRating == null || count == 0) {
            return "بدون امتیاز";
        }
        return String.format("⭐ %.1f (%d رأی)", averageRating, count);
    }

    /**
     * Checks whether the "editable" condition holds.
     *
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     */
    public boolean isEditable() {
        return isPending() || isActive();
    }

    /**
     * Checks whether the "deletable" condition holds.
     *
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     */
    public boolean isDeletable() {
        return isPending() || isActive();
    }

    /**
     * Performs the "to string" operation.
     *
     * @return the resulting string
     */
    @Override
    public String toString() {
        return title + " - " + getFormattedPrice();
    }

    /**
     * Checks whether the "purchased by" condition holds.
     *
     * @param userId id of the user
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     */
    public boolean isPurchasedBy(Long userId) {
        return isSold() && buyerId != null && buyerId.equals(userId);
    }
}