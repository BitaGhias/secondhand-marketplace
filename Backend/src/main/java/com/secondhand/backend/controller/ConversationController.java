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

/**
 * REST controller exposing the "conversation" API endpoints.
 * <p>
 * This class is the entry point for HTTP requests; it delegates the work to the service layer and returns the result as JSON with a proper status code. Errors are handled centrally by {@code GlobalExceptionHandler}.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
@RestController
@RequestMapping("/api/chat")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private CurrentUserService currentUserService;

    /**
     * Starts conversation.
     *
     * @param itemId id of the ad (item)
     * @return an HTTP response containing the operation result and a proper status code
     */
    @PostMapping("/conversation")
    public ResponseEntity<ConversationResponse> startConversation(@RequestParam Long itemId) {
        ConversationResponse response = conversationService.startConversation(
                itemId,
                currentUserService.getCurrentUserId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Sends message.
     *
     * @param request request body received from the client
     * @return an HTTP response containing the operation result and a proper status code
     */
    @PostMapping("/message")
    public ResponseEntity<ChatMessageResponse> sendMessage(@RequestBody ChatMessageRequest request) {
        ChatMessageResponse response = conversationService.sendMessage(
                request,
                currentUserService.getCurrentUserId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Gets messages.
     *
     * @param conversationId id of the conversation
     * @return an HTTP response containing the operation result and a proper status code
     */
    @GetMapping("/messages/{conversationId}")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(@PathVariable Long conversationId) {
        List<ChatMessageResponse> messages = conversationService.getMessages(
                conversationId,
                currentUserService.getCurrentUserId()
        );
        return ResponseEntity.ok(messages);
    }

    /**
     * Gets user conversations.
     *
     * @return an HTTP response containing the operation result and a proper status code
     */
    @GetMapping("/user")
    public ResponseEntity<List<ConversationResponse>> getUserConversations() {
        List<ConversationResponse> conversations = conversationService.getUserConversations(
                currentUserService.getCurrentUserId()
        );
        return ResponseEntity.ok(conversations);
    }

    /**
     * Edits message.
     *
     * @param id unique identifier of the record
     * @param text the text value
     * @return an HTTP response containing the operation result and a proper status code
     */
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

    /**
     * Deletes message.
     *
     * @param id unique identifier of the record
     * @return an HTTP response containing the operation result and a proper status code
     */
    @DeleteMapping("/message/{id}")
    public ResponseEntity<ChatMessageResponse> deleteMessage(@PathVariable Long id) {
        ChatMessageResponse response = conversationService.deleteMessage(
                id,
                currentUserService.getCurrentUserId()
        );
        return ResponseEntity.ok(response);
    }
}