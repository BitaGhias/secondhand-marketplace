package com.secondhand.backend.controller;

import com.secondhand.backend.entity.User;
import com.secondhand.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
