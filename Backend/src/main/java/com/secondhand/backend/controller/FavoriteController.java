package com.secondhand.backend.controller;

import com.secondhand.backend.entity.Favorite;
import com.secondhand.backend.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    public FavoriteService favoriteService;

    @PostMapping("/add")
    public ResponseEntity<?> addFavorite(@RequestParam Long userId, @RequestParam Long itemId) {
        try {
            Favorite favorite = favoriteService.addFavorite(userId, itemId);
            return ResponseEntity.ok(favorite);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/remove")
    public ResponseEntity<?> removeFavorite(@RequestParam Long userId, @RequestParam Long itemId) {
        try {
            favoriteService.removeFavorite(userId, itemId);
            return ResponseEntity.ok("آگهی با موفقیت از لیست علاقه‌مندی‌ها حذف شد.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Favorite>> getUserFavorites(@PathVariable Long userId) {
        return ResponseEntity.ok(favoriteService.getUserFavorites(userId));
    }
}