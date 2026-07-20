package com.secondhand.backend.controller;

import com.secondhand.backend.dto.rating.RatingCreateRequest;
import com.secondhand.backend.dto.rating.RatingResponse;
import com.secondhand.backend.service.RatingService;
import com.secondhand.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    @Autowired
    private RatingService ratingService;

    @Autowired
    private UserService userService;

    private Long getCurrentUserId() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        return userService.getUserIdByUsername(username);
    }

    @PostMapping("/add")
    public ResponseEntity<RatingResponse> addRating(@RequestBody RatingCreateRequest request) {
        Long raterId = getCurrentUserId();
        RatingResponse response = ratingService.addRating(request, raterId);
        return ResponseEntity.ok(response);
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