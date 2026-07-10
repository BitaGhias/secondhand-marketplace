package com.secondhand.backend.controller;

import com.secondhand.backend.dto.FavoriteRequest;
import com.secondhand.backend.dto.FavoriteResponse;
import com.secondhand.backend.service.FavoriteService;
import com.secondhand.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private UserService userService;

    private Long getCurrentUserId() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        return userService.getUserIdByUsername(username);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addFavorite(@RequestBody FavoriteRequest request) {
        try {
            Long userId = getCurrentUserId();
            FavoriteResponse response = favoriteService.addFavorite(request, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/remove")
    public ResponseEntity<?> removeFavorite(@RequestBody FavoriteRequest request) {
        try {
            Long userId = getCurrentUserId();
            favoriteService.removeFavorite(request, userId);
            return ResponseEntity.ok("آگهی با موفقیت از لیست علاقه‌مندی‌ها حذف شد.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user")
    public ResponseEntity<List<FavoriteResponse>> getUserFavorites() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(favoriteService.getUserFavorites(userId));
    }
}