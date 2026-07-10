package com.secondhand.backend.dto;

public class CommentResponse {
    private Long id;
    private String text;
    private Long itemId;
    private String itemTitle;
    private Long userId;
    private String username;

    public CommentResponse() {}

    public CommentResponse(Long id, String text, Long itemId, String itemTitle, Long userId, String username) {
        this.id = id;
        this.text = text;
        this.itemId = itemId;
        this.itemTitle = itemTitle;
        this.userId = userId;
        this.username = username;
    }

    public Long getId() { return id; }
    public String getText() { return text; }
    public Long getItemId() { return itemId; }
    public String getItemTitle() { return itemTitle; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }

    public void setId(Long id) { this.id = id; }
    public void setText(String text) { this.text = text; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public void setItemTitle(String itemTitle) { this.itemTitle = itemTitle; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
}