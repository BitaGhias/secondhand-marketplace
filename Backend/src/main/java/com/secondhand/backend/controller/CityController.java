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

@RestController
@RequestMapping("/api/cities")
public class CityController {

    @Autowired
    private CityService cityService;

    @Autowired
    private CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<List<CityResponse>> getAllCities() {
        return ResponseEntity.ok(cityService.getAllCities());
    }

    @PostMapping("/add")
    public ResponseEntity<CityResponse> addCity(@RequestBody CityRequest request) {
        CityResponse cityResponse = cityService.addCity(
                currentUserService.getCurrentUserId(),
                request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(cityResponse);
    }
}