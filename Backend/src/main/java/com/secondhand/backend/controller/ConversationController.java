package com.secondhand.backend.controller;

import com.secondhand.backend.entity.ChatMessage;
import com.secondhand.backend.entity.Conversation;
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

    //ایجاد یا ورود به مکالمه
    @PostMapping("/conversation")
    public ResponseEntity<?> startConversation(@RequestParam Long itemId, @RequestParam Long buyerId) {
        try {
            Conversation conversation = conversationService.startConversation(itemId, buyerId);
            return ResponseEntity.ok(conversation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/message")
    public ResponseEntity<?> sendMessage(
            @RequestParam Long conversationId,
            @RequestParam Long senderId,
            @RequestParam String text) {
        try {
            ChatMessage message = conversationService.sendMessage(conversationId, senderId, text);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/messages/{conversationId}")
    public ResponseEntity<List<ChatMessage>> getMessages(@PathVariable Long conversationId) {
        return ResponseEntity.ok(conversationService.getMessages(conversationId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Conversation>> getUserConversations(@PathVariable Long userId) {
        return ResponseEntity.ok(conversationService.getUserConversations(userId));
    }
}