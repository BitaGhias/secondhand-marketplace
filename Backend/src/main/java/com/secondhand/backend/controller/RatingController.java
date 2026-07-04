package com.secondhand.backend.controller;

import com.secondhand.backend.entity.Rating;
import com.secondhand.backend.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    @Autowired
    public RatingService ratingService;

    @PostMapping("/add")
    public ResponseEntity<?> addRating(
            @RequestParam Long itemId,
            @RequestParam Long raterId,
            @RequestParam int score,
            @RequestParam(required = false) String comment) {
        try {
            Rating rating = ratingService.addRating(itemId, raterId, score, comment);
            return ResponseEntity.ok(rating);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/seller/{sellerId}/average")
    public ResponseEntity<Double> getSellerAverage(@PathVariable Long sellerId) {
        double average = ratingService.getSellerAverageRating(sellerId);
        return ResponseEntity.ok(average);
    }
}