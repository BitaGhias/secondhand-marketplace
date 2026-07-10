package com.secondhand.backend.dto;

import java.time.LocalDateTime;

public class ChatMessageResponse {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderUsername;
    private String text;
    private LocalDateTime timestamp;

    public ChatMessageResponse() {}

    public ChatMessageResponse(Long id, Long conversationId, Long senderId, String senderUsername,
                               String text, LocalDateTime timestamp) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.text = text;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }
    public Long getConversationId() { return conversationId; }
    public Long getSenderId() { return senderId; }
    public String getSenderUsername() { return senderUsername; }
    public String getText() { return text; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public void setId(Long id) { this.id = id; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }
    public void setText(String text) { this.text = text; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}