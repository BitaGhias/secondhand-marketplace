package com.secondhand.backend.dto.favorite;

import jakarta.validation.constraints.NotNull;

public class FavoriteRequest {
    @NotNull(message = "شناسه آگهی الزامی است")
    private Long itemId;

    public FavoriteRequest() {}
    public FavoriteRequest(Long itemId) { this.itemId = itemId; }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
}