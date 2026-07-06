package com.secondhand.backend.controller;

import com.secondhand.backend.dto.UserRegisterRequest;
import com.secondhand.backend.dto.UserResponse;
import com.secondhand.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegisterRequest request) {
        try {
            UserResponse response = userService.registerUser(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestParam String username, @RequestParam String password) {
        try {
            UserResponse response = userService.loginUser(username, password);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllUsers(@RequestParam Long adminId) {
        try {
            List<UserResponse> users = userService.getAllUsers(adminId);
            return ResponseEntity.ok(users);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/admin/toggle-block")
    public ResponseEntity<?> toggleBlock(
            @RequestParam Long adminId,
            @RequestParam Long userId,
            @RequestParam boolean block) {
        try {
            UserResponse response = userService.toggleUserBlockStatus(adminId, userId, block);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}