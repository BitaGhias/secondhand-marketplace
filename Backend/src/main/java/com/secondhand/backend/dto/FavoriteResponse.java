package com.secondhand.backend.dto;

public class FavoriteResponse {
    private Long id;
    private Long itemId;
    private String itemTitle;
    private Long itemPrice;
    private String itemStatus;
    private Long userId;

    public FavoriteResponse() {}

    public FavoriteResponse(Long id, Long itemId, String itemTitle, Long itemPrice, String itemStatus, Long userId) {
        this.id = id;
        this.itemId = itemId;
        this.itemTitle = itemTitle;
        this.itemPrice = itemPrice;
        this.itemStatus = itemStatus;
        this.userId = userId;
    }

    public Long getId() { return id; }
    public Long getItemId() { return itemId; }
    public String getItemTitle() { return itemTitle; }
    public Long getItemPrice() { return itemPrice; }
    public String getItemStatus() { return itemStatus; }
    public Long getUserId() { return userId; }

    public void setId(Long id) { this.id = id; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public void setItemTitle(String itemTitle) { this.itemTitle = itemTitle; }
    public void setItemPrice(Long itemPrice) { this.itemPrice = itemPrice; }
    public void setItemStatus(String itemStatus) { this.itemStatus = itemStatus; }
    public void setUserId(Long userId) { this.userId = userId; }
}