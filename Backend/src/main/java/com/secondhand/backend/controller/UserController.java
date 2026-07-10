package com.secondhand.backend.controller;

import com.secondhand.backend.dto.*;
import com.secondhand.backend.service.UserService;
import com.secondhand.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    private Long getCurrentUserId() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        return userService.getUserIdByUsername(username);
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@RequestBody UserRegisterRequest request) {
        UserResponse response = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@RequestBody LoginRequest request) {
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
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        Long adminId = getCurrentUserId();
        List<UserResponse> users = userService.getAllUsers(adminId);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/admin/toggle-block")
    public ResponseEntity<UserResponse> toggleBlock(
            @RequestParam Long userId,
            @RequestParam boolean block) {
        Long adminId = getCurrentUserId();
        UserResponse response = userService.toggleUserBlockStatus(adminId, userId, block);
        return ResponseEntity.ok(response);
    }
}