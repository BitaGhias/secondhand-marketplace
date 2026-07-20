package com.secondhand.backend.dto.chat;

import java.time.LocalDateTime;

public class ConversationResponse {
    private Long id;
    private Long itemId;
    private String itemTitle;
    private Long buyerId;
    private String buyerUsername;
    private Long sellerId;
    private String sellerUsername;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Long unreadCount;

    public ConversationResponse() {}

    public ConversationResponse(Long id, Long itemId, String itemTitle, Long buyerId,
                                String buyerUsername, Long sellerId, String sellerUsername,
                                String lastMessage, LocalDateTime lastMessageTime, Long unreadCount) {
        this.id = id;
        this.itemId = itemId;
        this.itemTitle = itemTitle;
        this.buyerId = buyerId;
        this.buyerUsername = buyerUsername;
        this.sellerId = sellerId;
        this.sellerUsername = sellerUsername;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.unreadCount = unreadCount;
    }

    public Long getId() { return id; }
    public Long getItemId() { return itemId; }
    public String getItemTitle() { return itemTitle; }
    public Long getBuyerId() { return buyerId; }
    public String getBuyerUsername() { return buyerUsername; }
    public Long getSellerId() { return sellerId; }
    public String getSellerUsername() { return sellerUsername; }
    public String getLastMessage() { return lastMessage; }
    public LocalDateTime getLastMessageTime() { return lastMessageTime; }
    public Long getUnreadCount() { return unreadCount; }

    public void setId(Long id) { this.id = id; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public void setItemTitle(String itemTitle) { this.itemTitle = itemTitle; }
    public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }
    public void setBuyerUsername(String buyerUsername) { this.buyerUsername = buyerUsername; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }
    public void setSellerUsername(String sellerUsername) { this.sellerUsername = sellerUsername; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public void setLastMessageTime(LocalDateTime lastMessageTime) { this.lastMessageTime = lastMessageTime; }
    public void setUnreadCount(Long unreadCount) { this.unreadCount = unreadCount; }
}