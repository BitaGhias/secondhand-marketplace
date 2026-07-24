package com.secondhand.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_chat_messages_conversation_time", columnList = "conversation_id, timestamp"),
        @Index(name = "idx_chat_messages_conversation_read", columnList = "conversation_id, is_read")
})
/**
 * JPA entity representing a "chat message" record in the database.
 * <p>
 * This class defines the structure of the matching table in the SQLite database and is managed by Hibernate; relations between tables are declared with JPA annotations.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String text;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "is_edited", nullable = false)
    private boolean isEdited = false;

    @ManyToOne
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /**
     * Creates a new {@code ChatMessage} instance.
     */
    public ChatMessage() {}

    public ChatMessage(Long id, String text, LocalDateTime timestamp, boolean isRead,
                       boolean isDeleted, boolean isEdited, Conversation conversation, User sender) {
        this.id = id;
        this.text = text;
        this.timestamp = timestamp;
        this.isRead = isRead;
        this.isDeleted = isDeleted;
        this.isEdited = isEdited;
        this.conversation = conversation;
        this.sender = sender;
    }

    public Long getId() { return id; }
    public String getText() { return text; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public boolean isRead() { return isRead; }
    public boolean isDeleted() { return isDeleted; }
    public boolean isEdited() { return isEdited; }
    public Conversation getConversation() { return conversation; }
    public User getSender() { return sender; }

    public void setId(Long id) { this.id = id; }
    public void setText(String text) { this.text = text; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public void setRead(boolean read) { isRead = read; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }
    public void setEdited(boolean edited) { isEdited = edited; }
    public void setConversation(Conversation conversation) { this.conversation = conversation; }
    public void setSender(User sender) { this.sender = sender; }
}