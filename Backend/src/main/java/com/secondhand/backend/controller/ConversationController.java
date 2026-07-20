package com.secondhand.backend.controller;

import com.secondhand.backend.dto.chat.ChatMessageRequest;
import com.secondhand.backend.dto.chat.ChatMessageResponse;
import com.secondhand.backend.dto.chat.ConversationResponse;
import com.secondhand.backend.security.CurrentUserService;
import com.secondhand.backend.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private CurrentUserService currentUserService;

    @PostMapping("/conversation")
    public ResponseEntity<ConversationResponse> startConversation(@RequestParam Long itemId) {
        ConversationResponse response = conversationService.startConversation(
                itemId,
                currentUserService.getCurrentUserId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/message")
    public ResponseEntity<ChatMessageResponse> sendMessage(@RequestBody ChatMessageRequest request) {
        ChatMessageResponse response = conversationService.sendMessage(
                request,
                currentUserService.getCurrentUserId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/messages/{conversationId}")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(@PathVariable Long conversationId) {
        List<ChatMessageResponse> messages = conversationService.getMessages(
                conversationId,
                currentUserService.getCurrentUserId()
        );
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/user")
    public ResponseEntity<List<ConversationResponse>> getUserConversations() {
        List<ConversationResponse> conversations = conversationService.getUserConversations(
                currentUserService.getCurrentUserId()
        );
        return ResponseEntity.ok(conversations);
    }

    @PutMapping("/message/{id}")
    public ResponseEntity<ChatMessageResponse> editMessage(
            @PathVariable Long id,
            @RequestParam String text
    ) {
        ChatMessageResponse response = conversationService.editMessage(
                id,
                currentUserService.getCurrentUserId(),
                text
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/message/{id}")
    public ResponseEntity<ChatMessageResponse> deleteMessage(@PathVariable Long id) {
        ChatMessageResponse response = conversationService.deleteMessage(
                id,
                currentUserService.getCurrentUserId()
        );
        return ResponseEntity.ok(response);
    }
}