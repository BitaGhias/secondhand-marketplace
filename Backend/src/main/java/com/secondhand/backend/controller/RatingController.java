package com.secondhand.backend.controller;

import com.secondhand.backend.dto.rating.RatingCreateRequest;
import com.secondhand.backend.dto.rating.RatingResponse;
import com.secondhand.backend.security.CurrentUserService;
import com.secondhand.backend.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    @Autowired
    private RatingService ratingService;

    @Autowired
    private CurrentUserService currentUserService;

    @PostMapping("/add")
    public ResponseEntity<RatingResponse> addRating(@RequestBody RatingCreateRequest request) {
        RatingResponse response = ratingService.addRating(
                request,
                currentUserService.getCurrentUserId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/seller/{sellerId}/average")
    public ResponseEntity<Double> getSellerAverage(@PathVariable Long sellerId) {
        double average = ratingService.getSellerAverageRating(sellerId);
        return ResponseEntity.ok(average);
    }

    @GetMapping("/seller/{sellerId}/count")
    public ResponseEntity<Long> getSellerRatingCount(@PathVariable Long sellerId) {
        long count = ratingService.getSellerRatingCount(sellerId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<RatingResponse>> getSellerRatings(@PathVariable Long sellerId) {
        List<RatingResponse> ratings = ratingService.getSellerRatings(sellerId);
        return ResponseEntity.ok(ratings);
    }
}