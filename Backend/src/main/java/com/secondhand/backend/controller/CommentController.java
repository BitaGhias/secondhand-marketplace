package com.secondhand.backend.controller;

import com.secondhand.backend.entity.Comment;
import com.secondhand.backend.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    public CommentService commentService;

    //  آدرس اینترنتی برای فرستادن کامنت جدید (POST)
    @PostMapping("/add")
    public ResponseEntity<?> addComment(
            @RequestParam Long itemId,
            @RequestParam Long userId,
            @RequestParam String text) {
        try {
            Comment comment = commentService.addComment(itemId, userId, text);
            return ResponseEntity.ok(comment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //  آدرس اینترنتی برای گرفتن کامنت‌های یک آگهی (GET)
    @GetMapping("/item/{itemId}")
    public ResponseEntity<List<Comment>> getCommentsByItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(commentService.getCommentsByItem(itemId));
    }
}