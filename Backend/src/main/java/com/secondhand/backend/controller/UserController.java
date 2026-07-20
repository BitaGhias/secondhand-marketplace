package com.secondhand.backend.controller;

import com.secondhand.backend.dto.user.*;
import com.secondhand.backend.service.UserService;
import com.secondhand.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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
        return userService.getUserIdByUsername(userDetails.getUsername());
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@RequestBody UserRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@RequestBody LoginRequest request) {
        UserResponse userResponse = userService.loginUser(request.getUsername(), request.getPassword());
        String token = jwtUtil.generateToken(userResponse.getUsername(), userResponse.getId(), userResponse.getRole().name());
        return ResponseEntity.ok(new LoginResponse(userResponse, token));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getMyProfile() {
        return ResponseEntity.ok(userService.getUserById(getCurrentUserId()));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateMyProfile(@RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUserProfile(getCurrentUserId(), request));
    }

    @PutMapping("/change-password")
    public ResponseEntity<UserResponse> changePassword(@RequestParam String oldPassword, @RequestParam String newPassword) {
        return ResponseEntity.ok(userService.changePassword(getCurrentUserId(), oldPassword, newPassword));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers(getCurrentUserId()));
    }

    @PostMapping("/admin/toggle-block")
    public ResponseEntity<UserResponse> toggleBlock(@RequestParam Long userId, @RequestParam boolean block) {
        return ResponseEntity.ok(userService.toggleUserBlockStatus(getCurrentUserId(), userId, block));
    }

    @PostMapping("/admin/make-admin")
    public ResponseEntity<UserResponse> makeAdmin(@RequestParam Long userId) {
        return ResponseEntity.ok(userService.makeAdmin(getCurrentUserId(), userId));
    }

    @GetMapping("/admin/is-admin")
    public ResponseEntity<Boolean> isAdmin() {
        // وابستگی به UserRepository حذف شد و از سرویس استفاده می‌شود
        return ResponseEntity.ok(userService.isAdmin(getCurrentUserId()));
    }

    @PostMapping(value = "/profile/image", consumes = "multipart/form-data")
    public ResponseEntity<UserResponse> uploadProfileImage(@RequestParam("image") MultipartFile image) {
        return ResponseEntity.ok(userService.updateProfileImage(getCurrentUserId(), image));
    }
}