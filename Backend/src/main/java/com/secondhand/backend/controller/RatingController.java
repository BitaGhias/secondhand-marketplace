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

/**
 * REST controller exposing the "rating" API endpoints.
 * <p>
 * This class is the entry point for HTTP requests; it delegates the work to the service layer and returns the result as JSON with a proper status code. Errors are handled centrally by {@code GlobalExceptionHandler}.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    @Autowired private RatingService ratingService;
    @Autowired private CurrentUserService currentUserService;

    /**
     * Adds rating.
     *
     * @param request request body received from the client
     * @return an HTTP response containing the operation result and a proper status code
     */
    @PostMapping("/add")
    public ResponseEntity<RatingResponse> addRating(@Valid @RequestBody RatingCreateRequest request) {
        RatingResponse response = ratingService.addRating(request, currentUserService.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Gets seller average.
     *
     * @param sellerId id of the seller
     * @return an HTTP response containing the operation result and a proper status code
     */
    @GetMapping("/seller/{sellerId}/average")
    public ResponseEntity<Double> getSellerAverage(@PathVariable Long sellerId) {
        return ResponseEntity.ok(ratingService.getSellerAverageRating(sellerId));
    }

    /**
     * Gets seller rating count.
     *
     * @param sellerId id of the seller
     * @return an HTTP response containing the operation result and a proper status code
     */
    @GetMapping("/seller/{sellerId}/count")
    public ResponseEntity<Long> getSellerRatingCount(@PathVariable Long sellerId) {
        return ResponseEntity.ok(ratingService.getSellerRatingCount(sellerId));
    }

    /**
     * Gets seller ratings.
     *
     * @param sellerId id of the seller
     * @return an HTTP response containing the operation result and a proper status code
     */
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<RatingResponse>> getSellerRatings(@PathVariable Long sellerId) {
        return ResponseEntity.ok(ratingService.getSellerRatings(sellerId));
    }

    /**
     * Checks whether the "rated" condition holds.
     *
     * @param itemId id of the ad (item)
     * @return an HTTP response containing the operation result and a proper status code
     */
    @GetMapping("/item/{itemId}/rated")
    public ResponseEntity<Boolean> hasRated(@PathVariable Long itemId) {
        Long userId = currentUserService.getCurrentUserId();
        boolean rated = ratingService.hasUserRatedItem(userId, itemId);
        return ResponseEntity.ok(rated);
    }
}