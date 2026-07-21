package com.secondhand.frontend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Comment {
    private Long id;
    private String text;          // فیلد اصلی در بک‌اند
    private Long itemId;
    private String itemTitle;
    private Long userId;
    private String username;
    private String createdAt;     // LocalDateTime به صورت String دسریالایز می‌شه

    public Comment() {}

    // ===== Getters =====
    public Long getId() { return id; }
    public String getText() { return text; }
    public Long getItemId() { return itemId; }
    public String getItemTitle() { return itemTitle; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getCreatedAt() { return createdAt; }

    // ===== Setters =====
    public void setId(Long id) { this.id = id; }
    public void setText(String text) { this.text = text; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public void setItemTitle(String itemTitle) { this.itemTitle = itemTitle; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    /** نمایش تاریخ کوتاه (۱۰ کاراکتر اول) */
    public String getShortDate() {
        if (createdAt == null || createdAt.length() < 10) return "";
        return createdAt.substring(0, 10);
    }
}
