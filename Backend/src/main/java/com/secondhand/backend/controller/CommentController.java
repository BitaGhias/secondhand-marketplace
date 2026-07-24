package com.secondhand.backend.controller;

import com.secondhand.backend.dto.comment.CommentCreateRequest;
import com.secondhand.backend.dto.comment.CommentResponse;
import com.secondhand.backend.security.CurrentUserService;
import com.secondhand.backend.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST controller exposing the "comment" API endpoints.
 * <p>
 * This class is the entry point for HTTP requests; it delegates the work to the service layer and returns the result as JSON with a proper status code. Errors are handled centrally by {@code GlobalExceptionHandler}.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CurrentUserService currentUserService;

    /**
     * Adds comment.
     *
     * @param request request body received from the client
     * @return an HTTP response containing the operation result and a proper status code
     */
    @PostMapping("/add")
    public ResponseEntity<CommentResponse> addComment(@Valid @RequestBody CommentCreateRequest request) {
        CommentResponse response = commentService.addComment(
                request, currentUserService.getCurrentUserId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Gets comments by item.
     *
     * @param itemId id of the ad (item)
     * @return an HTTP response containing the operation result and a proper status code
     */
    @GetMapping("/item/{itemId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(commentService.getCommentsByItem(itemId));
    }

    /**
     * Updates comment.
     *
     * @param id unique identifier of the record
     * @param text the text value
     * @return an HTTP response containing the operation result and a proper status code
     */
    @PutMapping("/{id}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long id,
            @RequestParam String text
    ) {
        return ResponseEntity.ok(
                commentService.updateComment(id, currentUserService.getCurrentUserId(), text)
        );
    }

    // FIX: 200 OK -> 204 No Content
    /**
     * Deletes comment.
     *
     * @param id unique identifier of the record
     * @return an HTTP response containing the operation result and a proper status code
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id, currentUserService.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}