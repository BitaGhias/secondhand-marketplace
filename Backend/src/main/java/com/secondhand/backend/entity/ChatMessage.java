package com.secondhand.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String text;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    public ChatMessage() {}

    public ChatMessage(Long id, String text, LocalDateTime timestamp, Conversation conversation, User sender) {
        this.id = id;
        this.text = text;
        this.timestamp = timestamp;
        this.conversation = conversation;
        this.sender = sender;
    }

    public Long getId() { return id; }
    public String getText() { return text; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public Conversation getConversation() { return conversation; }
    public User getSender() { return sender; }

    public void setId(Long id) { this.id = id; }
    public void setText(String text) { this.text = text; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public void setConversation(Conversation conversation) { this.conversation = conversation; }
    public void setSender(User sender) { this.sender = sender; }
}