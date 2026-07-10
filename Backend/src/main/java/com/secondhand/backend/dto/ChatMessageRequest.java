package com.secondhand.backend.dto;

import lombok.Data;

@Data
public class ChatMessageRequest {
    private Long conversationId;
    private String text;
}