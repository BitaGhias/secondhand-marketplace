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
    private Long otherPartyId;
    private String otherPartyUsername;
    private boolean otherPartyBlocked;
    private String lastMessage;
    private String lastMessageTime;
    private String lastMessageFullTime;
    private String lastMessageSender;
    private long unreadCount;

    public ConversationResponse() {}

    public ConversationResponse(Long id, Long itemId, String itemTitle,
                                Long buyerId, String buyerUsername,
                                Long sellerId, String sellerUsername,
                                Long otherPartyId, String otherPartyUsername,
                                boolean otherPartyBlocked,
                                String lastMessage, String lastMessageTime,
                                String lastMessageFullTime, String lastMessageSender,
                                long unreadCount) {
        this.id = id;
        this.itemId = itemId;
        this.itemTitle = itemTitle;
        this.buyerId = buyerId;
        this.buyerUsername = buyerUsername;
        this.sellerId = sellerId;
        this.sellerUsername = sellerUsername;
        this.otherPartyId = otherPartyId;
        this.otherPartyUsername = otherPartyUsername;
        this.otherPartyBlocked = otherPartyBlocked;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.lastMessageFullTime = lastMessageFullTime;
        this.lastMessageSender = lastMessageSender;
        this.unreadCount = unreadCount;
    }

    public ConversationResponse(Long id, Long itemId, String itemTitle,
                                Long buyerId, String buyerUsername,
                                Long sellerId, String sellerUsername,
                                String lastMessage, LocalDateTime lastMessageTime,
                                long unreadCount) {
        this.id = id;
        this.itemId = itemId;
        this.itemTitle = itemTitle;
        this.buyerId = buyerId;
        this.buyerUsername = buyerUsername;
        this.sellerId = sellerId;
        this.sellerUsername = sellerUsername;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime != null ? lastMessageTime.toString() : null;
        this.unreadCount = unreadCount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public String getItemTitle() { return itemTitle; }
    public void setItemTitle(String itemTitle) { this.itemTitle = itemTitle; }
    public Long getBuyerId() { return buyerId; }
    public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }
    public String getBuyerUsername() { return buyerUsername; }
    public void setBuyerUsername(String buyerUsername) { this.buyerUsername = buyerUsername; }
    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }
    public String getSellerUsername() { return sellerUsername; }
    public void setSellerUsername(String sellerUsername) { this.sellerUsername = sellerUsername; }
    public Long getOtherPartyId() { return otherPartyId; }
    public void setOtherPartyId(Long otherPartyId) { this.otherPartyId = otherPartyId; }
    public String getOtherPartyUsername() { return otherPartyUsername; }
    public void setOtherPartyUsername(String otherPartyUsername) { this.otherPartyUsername = otherPartyUsername; }
    public boolean isOtherPartyBlocked() { return otherPartyBlocked; }
    public void setOtherPartyBlocked(boolean otherPartyBlocked) { this.otherPartyBlocked = otherPartyBlocked; }
    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public String getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(String lastMessageTime) { this.lastMessageTime = lastMessageTime; }
    public String getLastMessageFullTime() { return lastMessageFullTime; }
    public void setLastMessageFullTime(String lastMessageFullTime) { this.lastMessageFullTime = lastMessageFullTime; }
    public String getLastMessageSender() { return lastMessageSender; }
    public void setLastMessageSender(String lastMessageSender) { this.lastMessageSender = lastMessageSender; }
    public long getUnreadCount() { return unreadCount; }
    public void setUnreadCount(long unreadCount) { this.unreadCount = unreadCount; }
}