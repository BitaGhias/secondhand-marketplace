package com.secondhand.frontend.model;

public class ChatMessage {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderUsername;
    private String text;
    private String timestamp;
    private String shortTime;
    private String fullTime;
    private boolean read;
    private boolean deleted;
    private boolean edited;

    public ChatMessage() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public String getShortTime() { return shortTime; }
    public void setShortTime(String shortTime) { this.shortTime = shortTime; }
    public String getFullTime() { return fullTime; }
    public void setFullTime(String fullTime) { this.fullTime = fullTime; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
    public boolean isEdited() { return edited; }
    public void setEdited(boolean edited) { this.edited = edited; }
}