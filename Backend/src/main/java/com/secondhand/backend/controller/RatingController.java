package com.secondhand.backend.controller;

import com.secondhand.backend.dto.RatingCreateRequest;
import com.secondhand.backend.dto.RatingResponse;
import com.secondhand.backend.service.RatingService;
import com.secondhand.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> addRating(@RequestBody RatingCreateRequest request) {
        try {
            Long raterId = getCurrentUserId();
            RatingResponse response = ratingService.addRating(request, raterId);
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