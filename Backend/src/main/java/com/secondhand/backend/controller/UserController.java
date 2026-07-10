package com.secondhand.backend.controller;

import com.secondhand.backend.constant.Role;
import com.secondhand.backend.dto.*;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.exception.custom.ResourceNotFoundException;
import com.secondhand.backend.repository.UserRepository;
import com.secondhand.backend.service.UserService;
import com.secondhand.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController // کلاسی برای دریافت HTTP و برگرداندن پاسخ JSON
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

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

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getMyProfile() {
        Long userId = getCurrentUserId();
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateMyProfile(@RequestBody UserUpdateRequest request) {
        Long userId = getCurrentUserId();
        UserResponse response = userService.updateUserProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-password")
    public ResponseEntity<UserResponse> changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        Long userId = getCurrentUserId();
        UserResponse response = userService.changePassword(userId, oldPassword, newPassword);
        return ResponseEntity.ok(response);
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

    @PostMapping("/admin/make-admin")
    public ResponseEntity<UserResponse> makeAdmin(@RequestParam Long userId) {
        Long adminId = getCurrentUserId();
        UserResponse response = userService.makeAdmin(adminId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/is-admin")
    public ResponseEntity<Boolean> isAdmin() {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر یافت نشد")); // استثناعه چون کنترلر در اینجا مستقیم با ریپازیتوری کار میکنه
        return ResponseEntity.ok(user.getRole() == Role.ADMIN);
    }
}