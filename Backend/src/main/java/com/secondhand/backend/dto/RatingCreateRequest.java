package com.secondhand.backend.dto;

public class RatingCreateRequest {
    private Long itemId;
    private int score;
    private String comment;

    public RatingCreateRequest() {}
    public RatingCreateRequest(Long itemId, int score, String comment) {
        this.itemId = itemId;
        this.score = score;
        this.comment = comment;
    }

    public Long getItemId() { return itemId; }
    public int getScore() { return score; }
    public String getComment() { return comment; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public void setScore(int score) { this.score = score; }
    public void setComment(String comment) { this.comment = comment; }
}