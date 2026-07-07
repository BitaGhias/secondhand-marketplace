package com.secondhand.backend.controller;

import com.secondhand.backend.dto.*;
import com.secondhand.backend.service.UserService;
import com.secondhand.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegisterRequest request) {
        try {
            UserResponse response = userService.registerUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage(), 400));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request) {
        try {
            UserResponse userResponse = userService.loginUser(
                    request.getUsername(),
                    request.getPassword()
            );

            String token = jwtUtil.generateToken(
                    userResponse.getUsername(),
                    userResponse.getId(),
                    userResponse.getRole().name()
            );

            LoginResponse loginResponse = new LoginResponse(userResponse, token);
            return ResponseEntity.ok(loginResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(e.getMessage(), 401));
        }
    }

    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllUsers(@RequestParam Long adminId) {
        try {
            List<UserResponse> users = userService.getAllUsers(adminId);
            return ResponseEntity.ok(users);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(e.getMessage(), 403));
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
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(e.getMessage(), 403));
        }
    }
}