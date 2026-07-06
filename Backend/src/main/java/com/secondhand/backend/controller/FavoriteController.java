package com.secondhand.backend.controller;

import com.secondhand.backend.dto.FavoriteRequest;
import com.secondhand.backend.dto.FavoriteResponse;
import com.secondhand.backend.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @PostMapping("/add")
    public ResponseEntity<?> addFavorite(@RequestBody FavoriteRequest request) {
        try {
            FavoriteResponse response = favoriteService.addFavorite(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/remove")
    public ResponseEntity<?> removeFavorite(@RequestBody FavoriteRequest request) {
        try {
            favoriteService.removeFavorite(request);
            return ResponseEntity.ok("آگهی با موفقیت از لیست علاقه‌مندی‌ها حذف شد.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FavoriteResponse>> getUserFavorites(@PathVariable Long userId) {
        return ResponseEntity.ok(favoriteService.getUserFavorites(userId));
    }
}