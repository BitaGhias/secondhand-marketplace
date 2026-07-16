package com.secondhand.frontend.model;

import java.util.List;

public class Item {
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
    private List<Image> images;
    private String rejectionReason;
    private Double averageRating;
    private Long categoryId;
    private Long cityId;

    // ===== Constructors =====
    public Item() {}

    public Item(Long id, String title, String description, double price, String status,
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

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

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

    public List<Image> getImages() { return images; }
    public void setImages(List<Image> images) { this.images = images; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public Long getCityId() { return cityId; }
    public void setCityId(Long cityId) { this.cityId = cityId; }

    /**
     * قیمت را به صورت فرمت شده با کاما برمی‌گرداند
     * مثال: ۱,۸۰۰,۰۰۰ تومان
     */
    public String getFormattedPrice() {
        return String.format("%,d تومان", (long) price);
    }

    /**
     * قیمت را به صورت خلاصه برمی‌گرداند
     * مثال: ۱.۸ میلیون تومان
     */
    public String getShortPrice() {
        if (price >= 1_000_000_000) {
            return String.format("%.1f میلیارد تومان", price / 1_000_000_000);
        } else if (price >= 1_000_000) {
            return String.format("%.1f میلیون تومان", price / 1_000_000);
        } else if (price >= 1_000) {
            return String.format("%.1f هزار تومان", price / 1_000);
        } else {
            return String.format("%.0f تومان", price);
        }
    }

    /**
     * وضعیت آگهی را به فارسی برمی‌گرداند
     */
    public String getPersianStatus() {
        if (status == null) return "نامشخص";
        return switch (status.toUpperCase()) {
            case "PENDING" -> "در انتظار بررسی";
            case "ACTIVE" -> "فعال";
            case "SOLD" -> "فروخته شده";
            case "DELETED" -> "حذف شده";
            case "REJECTED" -> "رد شده";
            default -> status;
        };
    }

    /**
     * رنگ وضعیت برای نمایش در UI
     */
    public String getStatusColor() {
        if (status == null) return "#888888";
        return switch (status.toUpperCase()) {
            case "PENDING" -> "#f9a825";    // زرد
            case "ACTIVE" -> "#4caf50";     // سبز
            case "SOLD" -> "#2196f3";       // آبی
            case "DELETED" -> "#f44336";    // قرمز
            case "REJECTED" -> "#f44336";   // قرمز
            default -> "#888888";
        };
    }

    /**
     * آیا آگهی فعال است؟
     */
    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }

    /**
     * آیا آگهی در انتظار بررسی است؟
     */
    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(status);
    }

    /**
     * آیا آگهی فروخته شده است؟
     */
    public boolean isSold() {
        return "SOLD".equalsIgnoreCase(status);
    }

    /**
     * آیا آگهی متعلق به کاربر مشخصی است؟
     */
    public boolean isOwner(Long userId) {
        return ownerId != null && ownerId.equals(userId);
    }

    /**
     * عنوان کامل با دسته‌بندی
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
     * آیا آگهی تصویر دارد؟
     */
    public boolean hasImages() {
        return images != null && !images.isEmpty();
    }

    /**
     * تعداد تصاویر آگهی
     */
    public int getImageCount() {
        return images != null ? images.size() : 0;
    }

    /**
     * دریافت اولین تصویر (برای نمایش در لیست)
     */
    public String getFirstImageUrl() {
        if (hasImages()) {
            return images.get(0).getFullUrl();  // استفاده از getFullUrl()
        }
        return null;
    }

    /**
     * دریافت امتیاز به صورت فرمت شده
     */
    public String getFormattedRating() {
        if (averageRating == null) {
            return "بدون امتیاز";
        }
        return String.format("⭐ %.1f", averageRating);
    }

    /**
     * دریافت امتیاز با تعداد رأی
     */
    public String getRatingWithCount(int count) {
        if (averageRating == null || count == 0) {
            return "بدون امتیاز";
        }
        return String.format("⭐ %.1f (%d رأی)", averageRating, count);
    }

    /**
     * آیا آگهی قابل ویرایش است؟
     * (فقط آگهی‌های در انتظار بررسی و فعال قابل ویرایش هستند)
     */
    public boolean isEditable() {
        return isPending() || isActive();
    }

    /**
     * آیا آگهی قابل حذف است؟
     */
    public boolean isDeletable() {
        return isPending() || isActive();
    }

    @Override
    public String toString() {
        return title + " - " + getFormattedPrice();
    }
}