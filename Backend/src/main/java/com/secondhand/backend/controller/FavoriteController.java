package com.secondhand.backend.controller;

import com.secondhand.backend.dto.favorite.FavoriteRequest;
import com.secondhand.backend.dto.favorite.FavoriteResponse;
import com.secondhand.backend.security.CurrentUserService;
import com.secondhand.backend.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private CurrentUserService currentUserService;

    @PostMapping("/add")
    public ResponseEntity<FavoriteResponse> addFavorite(@RequestBody FavoriteRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        FavoriteResponse response = favoriteService.addFavorite(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/remove")
    public ResponseEntity<String> removeFavorite(@RequestBody FavoriteRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        favoriteService.removeFavorite(request, userId);
        return ResponseEntity.ok("آگهی با موفقیت از لیست علاقه‌مندی‌ها حذف شد.");
    }

    @GetMapping("/user")
    public ResponseEntity<List<FavoriteResponse>> getUserFavorites() {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(favoriteService.getUserFavorites(userId));
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkFavorite(@RequestParam Long itemId) {
        Long userId = currentUserService.getCurrentUserId();
        boolean isFavorite = favoriteService.isFavorite(userId, itemId);
        return ResponseEntity.ok(isFavorite);
    }
}