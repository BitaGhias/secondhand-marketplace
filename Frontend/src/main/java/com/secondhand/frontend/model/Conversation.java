package com.secondhand.frontend.model;

import java.util.List;

/**
 * Client-side model representing "conversation" data returned by the server.
 * <p>
 * This class is the client-side representation of data received from the server and is deserialized from JSON by Jackson.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class Conversation {
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
    private Long unreadCount;

    // Constructor پیش‌فرض
    public Conversation() {}

    // Getters & Setters
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
    /**
     * Sets last message.
     *
     * @param lastMessage the "last message" value of type {@code String}
     */
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public String getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(String lastMessageTime) { this.lastMessageTime = lastMessageTime; }
    public String getLastMessageFullTime() { return lastMessageFullTime; }
    public void setLastMessageFullTime(String lastMessageFullTime) { this.lastMessageFullTime = lastMessageFullTime; }
    public String getLastMessageSender() { return lastMessageSender; }
    public void setLastMessageSender(String lastMessageSender) { this.lastMessageSender = lastMessageSender; }
    public Long getUnreadCount() { return unreadCount; }
    public void setUnreadCount(long unreadCount) { this.unreadCount = unreadCount; }
    public void setUnreadCount(Long unreadCount) { this.unreadCount = unreadCount; }

    public String getOtherPartyUsername(Long myId) {
        if (myId == null) return "کاربر";
        if (buyerId != null && buyerId.equals(myId)) return sellerUsername;
        if (sellerId != null && sellerId.equals(myId)) return buyerUsername;
        return "کاربر";
    }
}