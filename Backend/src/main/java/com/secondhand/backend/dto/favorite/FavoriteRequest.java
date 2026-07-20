package com.secondhand.backend.dto.favorite;

public class FavoriteRequest {
    private Long itemId;

    public FavoriteRequest() {}
    public FavoriteRequest(Long itemId) { this.itemId = itemId; }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
}