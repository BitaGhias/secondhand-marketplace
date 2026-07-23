package com.secondhand.backend.dto.chat;

public class ChatMessageRequest {
    private Long conversationId;
    private String text;

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