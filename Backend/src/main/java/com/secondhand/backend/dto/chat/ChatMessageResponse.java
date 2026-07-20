package com.secondhand.backend.dto.chat;

import java.time.LocalDateTime;

public class ChatMessageResponse {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderUsername;
    private String text;
    private LocalDateTime timestamp;
    private boolean isRead;
    private boolean isDeleted;
    private boolean isEdited;

    public ChatMessageResponse() {}

    public ChatMessageResponse(Long id, Long conversationId, Long senderId, String senderUsername,
                               String text, LocalDateTime timestamp, boolean isRead,
                               boolean isDeleted, boolean isEdited) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.text = text;
        this.timestamp = timestamp;
        this.isRead = isRead;
        this.isDeleted = isDeleted;
        this.isEdited = isEdited;
    }

    public Long getId() { return id; }
    public Long getConversationId() { return conversationId; }
    public Long getSenderId() { return senderId; }
    public String getSenderUsername() { return senderUsername; }
    public String getText() { return text; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public boolean isRead() { return isRead; }
    public boolean isDeleted() { return isDeleted; }
    public boolean isEdited() { return isEdited; }

    public void setId(Long id) { this.id = id; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }
    public void setText(String text) { this.text = text; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public void setRead(boolean read) { isRead = read; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }
    public void setEdited(boolean edited) { isEdited = edited; }
}