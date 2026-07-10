package com.secondhand.backend.controller;

import com.secondhand.backend.dto.CommentCreateRequest;
import com.secondhand.backend.dto.CommentResponse;
import com.secondhand.backend.service.CommentService;
import com.secondhand.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    private Long getCurrentUserId() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        return userService.getUserIdByUsername(username);
    }

    @PostMapping("/add")
    public ResponseEntity<CommentResponse> addComment(@RequestBody CommentCreateRequest request) {
        Long userId = getCurrentUserId();
        CommentResponse response = commentService.addComment(request, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/item/{itemId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(commentService.getCommentsByItem(itemId));
    }
}