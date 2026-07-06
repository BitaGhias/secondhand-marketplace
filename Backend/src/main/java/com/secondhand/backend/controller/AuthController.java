package com.secondhand.backend.controller;

import com.secondhand.backend.entity.User;
import com.secondhand.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController //برای پاسخ به درخواست های فرانت
@RequestMapping("/api/auth")
public class AuthController
{
    @Autowired
    private UserService userService;

    @PostMapping("/register") //در این صورت این متد فعال شود
    public ResponseEntity<?> registerUser(@RequestBody User user)
    {
        try
        {
            userService.registerUser(user);
            return ResponseEntity.ok("ثبت نام موفق");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User loginRequest) {
        try {
            User user = userService.loginUser(loginRequest.getUsername(), loginRequest.getPassword());
            //  اگر موفق بود، کل مشخصات کاربر (مثل آیدی و نقش) رو به فرانت‌اَند پس میدیم
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllUsers(@RequestParam Long adminId) {
        try {
            List<User> users = userService.getAllUsers(adminId);
            return ResponseEntity.ok(users);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //  آدرس مسدود یا باز کردن حساب کاربر
    @PostMapping("/admin/toggle-block")
    public ResponseEntity<?> toggleBlock(
            @RequestParam Long adminId,
            @RequestParam Long userId,
            @RequestParam boolean block) {
        try {
            User updatedUser = userService.toggleUserBlockStatus(adminId, userId, block);
            String message = block ? "کاربر با موفقیت مسدود شد." : "کاربر با موفقیت رفع مسدودیت شد.";
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
