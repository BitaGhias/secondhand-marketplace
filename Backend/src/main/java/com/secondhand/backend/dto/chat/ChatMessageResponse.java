package com.secondhand.backend.dto.chat;

import java.time.LocalDateTime;

/**
 * Data Transfer Object carrying "chat message response" data between client and server.
 * <p>
 * This class is used purely for transferring data between client and server and is not mapped to the database directly, keeping the internal structure of the entities hidden from the client.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class ChatMessageResponse {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderUsername;
    private String text;
    private LocalDateTime timestamp;
    private String shortTime;
    private String fullTime;
    private boolean read;
    private boolean deleted;
    private boolean edited;

    /**
     * Creates a new {@code ChatMessageResponse} instance.
     */
    public ChatMessageResponse() {}

    public ChatMessageResponse(Long id, Long conversationId, Long senderId,
                               String senderUsername, String text,
                               LocalDateTime timestamp, String shortTime,
                               String fullTime, boolean read, boolean deleted,
                               boolean edited) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.text = text;
        this.timestamp = timestamp;
        this.shortTime = shortTime;
        this.fullTime = fullTime;
        this.read = read;
        this.deleted = deleted;
        this.edited = edited;
    }

    /**
     * Creates a new {@code ChatMessageResponse} instance.
     *
     * @param id unique identifier of the record
     * @param conversationId id of the conversation
     * @param senderId the "sender id" value of type {@code Long}
     * @param senderUsername the "sender username" value of type {@code String}
     * @param text the text value
     * @param timestamp the "timestamp" value of type {@code LocalDateTime}
     * @param read the "read" value of type {@code boolean}
     * @param deleted the "deleted" value of type {@code boolean}
     * @param edited the "edited" value of type {@code boolean}
     */
    public ChatMessageResponse(Long id, Long conversationId, Long senderId,
                               String senderUsername, String text,
                               LocalDateTime timestamp, boolean read,
                               boolean deleted, boolean edited) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.text = text;
        this.timestamp = timestamp;
        this.read = read;
        this.deleted = deleted;
        this.edited = edited;
    }

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
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
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