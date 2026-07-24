package com.secondhand.backend.dto.chat;

/**
 * Data Transfer Object carrying "chat message request" data between client and server.
 * <p>
 * This class is used purely for transferring data between client and server and is not mapped to the database directly, keeping the internal structure of the entities hidden from the client.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class ChatMessageRequest {
    private Long conversationId;
    private String text;

    /**
     * Creates a new {@code ChatMessageRequest} instance.
     */
    public ChatMessageRequest() {}
    public ChatMessageRequest(Long conversationId, String text) {
        this.conversationId = conversationId;
        this.text = text;
    }

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}