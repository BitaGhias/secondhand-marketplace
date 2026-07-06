package com.secondhand.backend.controller;

import com.secondhand.backend.dto.*;
import com.secondhand.backend.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ConversationController {

    @Autowired
    public ConversationService conversationService;

    @PostMapping("/conversation")
    public ResponseEntity<?> startConversation(@RequestParam Long itemId, @RequestParam Long buyerId) {
        try {
            ConversationResponse response = conversationService.startConversation(itemId, buyerId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/message")
    public ResponseEntity<?> sendMessage(@RequestBody ChatMessageRequest request) {
        try {
            ChatMessageResponse response = conversationService.sendMessage(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/messages/{conversationId}")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(@PathVariable Long conversationId) {
        return ResponseEntity.ok(conversationService.getMessages(conversationId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ConversationResponse>> getUserConversations(@PathVariable Long userId) {
        return ResponseEntity.ok(conversationService.getUserConversations(userId));
    }
}