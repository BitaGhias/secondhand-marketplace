package com.secondhand.backend.controller;

import com.secondhand.backend.dto.favorite.FavoriteRequest;
import com.secondhand.backend.dto.favorite.FavoriteResponse;
import com.secondhand.backend.security.CurrentUserService;
import com.secondhand.backend.service.FavoriteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST controller exposing the "favorite" API endpoints.
 * <p>
 * This class is the entry point for HTTP requests; it delegates the work to the service layer and returns the result as JSON with a proper status code. Errors are handled centrally by {@code GlobalExceptionHandler}.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private CurrentUserService currentUserService;

    /**
     * Adds favorite.
     *
     * @param request request body received from the client
     * @return an HTTP response containing the operation result and a proper status code
     */
    @PostMapping("/add")
    public ResponseEntity<FavoriteResponse> addFavorite(@Valid @RequestBody FavoriteRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        FavoriteResponse response = favoriteService.addFavorite(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // FIX: @RequestBody -> @RequestParam (HTTP spec)
    // FIX: 200 OK -> 204 No Content
    /**
     * Removes favorite.
     *
     * @param itemId id of the ad (item)
     * @return an HTTP response containing the operation result and a proper status code
     */
    @DeleteMapping("/remove")
    public ResponseEntity<Void> removeFavorite(@RequestParam Long itemId) {
        Long userId = currentUserService.getCurrentUserId();
        FavoriteRequest request = new FavoriteRequest();
        request.setItemId(itemId);
        favoriteService.removeFavorite(request, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Gets user favorites.
     *
     * @return an HTTP response containing the operation result and a proper status code
     */
    @GetMapping("/user")
    public ResponseEntity<List<FavoriteResponse>> getUserFavorites() {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(favoriteService.getUserFavorites(userId));
    }

    /**
     * Checks favorite.
     *
     * @param itemId id of the ad (item)
     * @return an HTTP response containing the operation result and a proper status code
     */
    @GetMapping("/check")
    public ResponseEntity<Boolean> checkFavorite(@RequestParam Long itemId) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(favoriteService.isFavorite(userId, itemId));
    }
}