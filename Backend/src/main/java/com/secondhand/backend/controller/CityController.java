package com.secondhand.backend.controller;

import com.secondhand.backend.dto.city.CityRequest;
import com.secondhand.backend.dto.city.CityResponse;
import com.secondhand.backend.security.CurrentUserService;
import com.secondhand.backend.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST controller exposing the "city" API endpoints.
 * <p>
 * This class is the entry point for HTTP requests; it delegates the work to the service layer and returns the result as JSON with a proper status code. Errors are handled centrally by {@code GlobalExceptionHandler}.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
@RestController
@RequestMapping("/api/cities")
public class CityController {

    @Autowired
    private CityService cityService;

    @Autowired
    private CurrentUserService currentUserService;

    /**
     * Gets all cities.
     *
     * @return an HTTP response containing the operation result and a proper status code
     */
    @GetMapping
    public ResponseEntity<List<CityResponse>> getAllCities() {
        return ResponseEntity.ok(cityService.getAllCities());
    }

    /**
     * Adds a new city; only admins are allowed and duplicate names are rejected with HTTP 400.
     *
     * @param request request body received from the client
     * @return an HTTP response containing the operation result and a proper status code
     */
    @PostMapping("/add")
    public ResponseEntity<CityResponse> addCity(@RequestBody CityRequest request) {
        CityResponse cityResponse = cityService.addCity(
                currentUserService.getCurrentUserId(),
                request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(cityResponse);
    }
}