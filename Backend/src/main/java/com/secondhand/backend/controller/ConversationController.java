package com.secondhand.backend.controller;

import com.secondhand.backend.dto.chat.ChatMessageRequest;
import com.secondhand.backend.dto.chat.ChatMessageResponse;
import com.secondhand.backend.dto.chat.ConversationResponse;
import com.secondhand.backend.service.ConversationService;
import com.secondhand.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private UserService userService;

    private Long getCurrentUserId() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        return userService.getUserIdByUsername(username);
    }

    @PostMapping("/conversation")
    public ResponseEntity<ConversationResponse> startConversation(@RequestParam Long itemId) {
        Long buyerId = getCurrentUserId();
        ConversationResponse response = conversationService.startConversation(itemId, buyerId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/message")
    public ResponseEntity<ChatMessageResponse> sendMessage(@RequestBody ChatMessageRequest request) {
        Long senderId = getCurrentUserId();
        ChatMessageResponse response = conversationService.sendMessage(request, senderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/messages/{conversationId}")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(@PathVariable Long conversationId) {
        Long userId = getCurrentUserId();
        List<ChatMessageResponse> messages = conversationService.getMessages(conversationId, userId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/user")
    public ResponseEntity<List<ConversationResponse>> getUserConversations() {
        Long userId = getCurrentUserId();
        List<ConversationResponse> conversations = conversationService.getUserConversations(userId);
        return ResponseEntity.ok(conversations);
    }

    @PutMapping("/message/{id}")
    public ResponseEntity<ChatMessageResponse> editMessage(
            @PathVariable Long id,
            @RequestParam String text) {
        Long userId = getCurrentUserId();
        ChatMessageResponse response = conversationService.editMessage(id, userId, text);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/message/{id}")
    public ResponseEntity<ChatMessageResponse> deleteMessage(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        ChatMessageResponse response = conversationService.deleteMessage(id, userId);
        return ResponseEntity.ok(response);
    }
}