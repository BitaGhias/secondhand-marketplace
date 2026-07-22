package com.secondhand.backend.controller;

import com.secondhand.backend.dto.rating.RatingCreateRequest;
import com.secondhand.backend.dto.rating.RatingResponse;
import com.secondhand.backend.security.CurrentUserService;
import com.secondhand.backend.service.RatingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    @Autowired private RatingService ratingService;
    @Autowired private CurrentUserService currentUserService;

    @PostMapping("/add")
    public ResponseEntity<RatingResponse> addRating(@Valid @RequestBody RatingCreateRequest request) {
        RatingResponse response = ratingService.addRating(request, currentUserService.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/seller/{sellerId}/average")
    public ResponseEntity<Double> getSellerAverage(@PathVariable Long sellerId) {
        return ResponseEntity.ok(ratingService.getSellerAverageRating(sellerId));
    }

    @GetMapping("/seller/{sellerId}/count")
    public ResponseEntity<Long> getSellerRatingCount(@PathVariable Long sellerId) {
        return ResponseEntity.ok(ratingService.getSellerRatingCount(sellerId));
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<RatingResponse>> getSellerRatings(@PathVariable Long sellerId) {
        return ResponseEntity.ok(ratingService.getSellerRatings(sellerId));
    }

    /**
     * بررسی اینکه کاربر جاری قبلاً به این آگهی امتیاز داده یا خیر
     * GET /api/ratings/item/{itemId}/rated
     */
    @GetMapping("/item/{itemId}/rated")
    public ResponseEntity<Boolean> hasRated(@PathVariable Long itemId) {
        Long userId = currentUserService.getCurrentUserId();
        boolean rated = ratingService.hasUserRatedItem(userId, itemId);
        return ResponseEntity.ok(rated);
    }
}