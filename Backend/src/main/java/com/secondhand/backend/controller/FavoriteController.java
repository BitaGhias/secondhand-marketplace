package com.secondhand.backend.controller;

import com.secondhand.backend.dto.favorite.FavoriteRequest;
import com.secondhand.backend.dto.favorite.FavoriteResponse;
import com.secondhand.backend.service.FavoriteService;
import com.secondhand.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<FavoriteResponse> addFavorite(@RequestBody FavoriteRequest request) {
        Long userId = getCurrentUserId();
        FavoriteResponse response = favoriteService.addFavorite(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/remove")
    public ResponseEntity<String> removeFavorite(@RequestBody FavoriteRequest request) {
        Long userId = getCurrentUserId();
        favoriteService.removeFavorite(request, userId);
        return ResponseEntity.ok("آگهی با موفقیت از لیست علاقه‌مندی‌ها حذف شد.");
    }

    @GetMapping("/user")
    public ResponseEntity<List<FavoriteResponse>> getUserFavorites() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(favoriteService.getUserFavorites(userId));
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkFavorite(@RequestParam Long itemId) {
        Long userId = getCurrentUserId();
        boolean isFavorite = favoriteService.isFavorite(userId, itemId);
        return ResponseEntity.ok(isFavorite);
    }
}