package com.secondhand.backend.controller;

import com.secondhand.backend.dto.*;
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
    public ResponseEntity<?> startConversation(@RequestParam Long itemId) {
        try {
            Long buyerId = getCurrentUserId();  // از JWT میگیریم
            ConversationResponse response = conversationService.startConversation(itemId, buyerId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/message")
    public ResponseEntity<?> sendMessage(@RequestBody ChatMessageRequest request) {
        try {
            Long senderId = getCurrentUserId();
            ChatMessageResponse response = conversationService.sendMessage(request, senderId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/messages/{conversationId}")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(@PathVariable Long conversationId) {
        return ResponseEntity.ok(conversationService.getMessages(conversationId));
    }

    @GetMapping("/user")
    public ResponseEntity<List<ConversationResponse>> getUserConversations() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(conversationService.getUserConversations(userId));
    }
}