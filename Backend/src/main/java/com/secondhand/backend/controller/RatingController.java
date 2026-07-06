package com.secondhand.backend.controller;

import com.secondhand.backend.dto.RatingCreateRequest;
import com.secondhand.backend.dto.RatingResponse;
import com.secondhand.backend.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    @Autowired
    private RatingService ratingService;

    @PostMapping("/add")
    public ResponseEntity<?> addRating(@RequestBody RatingCreateRequest request) {
        try {
            RatingResponse response = ratingService.addRating(request);
            return ResponseEntity.ok(response);
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