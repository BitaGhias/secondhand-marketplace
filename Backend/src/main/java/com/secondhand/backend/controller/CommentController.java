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

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CurrentUserService currentUserService;

    @PostMapping("/add")
    public ResponseEntity<CommentResponse> addComment(@Valid @RequestBody CommentCreateRequest request) {
        CommentResponse response = commentService.addComment(
                request, currentUserService.getCurrentUserId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/item/{itemId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(commentService.getCommentsByItem(itemId));
    }

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
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id, currentUserService.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}