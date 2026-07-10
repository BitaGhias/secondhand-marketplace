package com.secondhand.backend.dto;

public class CommentCreateRequest {
    private Long itemId;
    private String text;

    public CommentCreateRequest() {}
    public CommentCreateRequest(Long itemId, String text) {
        this.itemId = itemId;
        this.text = text;
    }

    public Long getItemId() { return itemId; }
    public String getText() { return text; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public void setText(String text) { this.text = text; }
}