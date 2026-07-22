package com.secondhand.backend.controller;

import com.secondhand.backend.dto.user.LoginRequest;
import com.secondhand.backend.dto.user.LoginResponse;
import com.secondhand.backend.dto.user.UserRegisterRequest;
import com.secondhand.backend.dto.user.UserResponse;
import com.secondhand.backend.dto.user.UserUpdateRequest;
import com.secondhand.backend.security.CurrentUserService;
import com.secondhand.backend.service.UserService;
import com.secondhand.backend.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CurrentUserService currentUserService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of("message", "با موفقیت خارج شدید. لطفاً توکن را در سمت کلاینت حذف کنید."));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginRequest request) {
        UserResponse userResponse = userService.loginUser(request.getUsername(), request.getPassword());
        String token = jwtUtil.generateToken(
                userResponse.getUsername(),
                userResponse.getId(),
                userResponse.getRole().name()
        );
        return ResponseEntity.ok(new LoginResponse(userResponse, token));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getMyProfile() {
        return ResponseEntity.ok(userService.getUserById(currentUserService.getCurrentUserId()));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateMyProfile(@Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(
                userService.updateUserProfile(currentUserService.getCurrentUserId(), request)
        );
    }

    // FIX: پسورد از @RequestParam (URL) به @RequestBody (JSON) منتقل شد
    @PutMapping("/change-password")
    public ResponseEntity<UserResponse> changePassword(
            @RequestBody Map<String, String> body
    ) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        return ResponseEntity.ok(
                userService.changePassword(
                        currentUserService.getCurrentUserId(),
                        oldPassword,
                        newPassword
                )
        );
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers(currentUserService.getCurrentUserId()));
    }

    @PostMapping("/admin/toggle-block")
    public ResponseEntity<UserResponse> toggleBlock(
            @RequestParam Long userId,
            @RequestParam boolean block
    ) {
        return ResponseEntity.ok(
                userService.toggleUserBlockStatus(
                        currentUserService.getCurrentUserId(),
                        userId,
                        block
                )
        );
    }

    @PostMapping("/admin/make-admin")
    public ResponseEntity<UserResponse> makeAdmin(@RequestParam Long userId) {
        return ResponseEntity.ok(
                userService.makeAdmin(currentUserService.getCurrentUserId(), userId)
        );
    }

    @GetMapping("/admin/is-admin")
    public ResponseEntity<Boolean> isAdmin() {
        return ResponseEntity.ok(userService.isAdmin(currentUserService.getCurrentUserId()));
    }

    @PostMapping(value = "/profile/image", consumes = "multipart/form-data")
    public ResponseEntity<UserResponse> uploadProfileImage(
            @RequestParam("image") MultipartFile image
    ) {
        return ResponseEntity.ok(
                userService.updateProfileImage(currentUserService.getCurrentUserId(), image)
        );
    }
}