package com.secondhand.backend.dto.comment;

import java.time.LocalDateTime;

/**
 * Data Transfer Object carrying "comment response" data between client and server.
 * <p>
 * This class is used purely for transferring data between client and server and is not mapped to the database directly, keeping the internal structure of the entities hidden from the client.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class CommentResponse {
    private Long id;
    private String text;
    private Long itemId;
    private String itemTitle;
    private Long userId;
    private String username;
    private LocalDateTime createdAt;
    // FIX (مورد ۴): نشانگر ویرایش‌شدن کامنت
    private boolean edited;

    /**
     * Creates a new {@code CommentResponse} instance.
     */
    public CommentResponse() {}

    public CommentResponse(Long id, String text, Long itemId, String itemTitle,
                           Long userId, String username, LocalDateTime createdAt, boolean edited) {
        this.id = id;
        this.text = text;
        this.itemId = itemId;
        this.itemTitle = itemTitle;
        this.userId = userId;
        this.username = username;
        this.createdAt = createdAt;
        this.edited = edited;
    }

    public Long getId() { return id; }
    public String getText() { return text; }
    public Long getItemId() { return itemId; }
    public String getItemTitle() { return itemTitle; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isEdited() { return edited; }

    public void setId(Long id) { this.id = id; }
    public void setText(String text) { this.text = text; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public void setItemTitle(String itemTitle) { this.itemTitle = itemTitle; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setEdited(boolean edited) { this.edited = edited; }
}