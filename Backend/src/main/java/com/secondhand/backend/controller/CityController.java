package com.secondhand.backend.controller;

import com.secondhand.backend.dto.CityRequest;
import com.secondhand.backend.dto.CityResponse;
import com.secondhand.backend.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/cities")
public class CityController {

    @Autowired
    private CityService cityService;

    @GetMapping
    public ResponseEntity<List<CityResponse>> getAllCities() {
        return ResponseEntity.ok(cityService.getAllCities());
    }

    @PostMapping("/add")
    public ResponseEntity<CityResponse> addCity(@RequestBody CityRequest request) {
        CityResponse cityResponse = cityService.addCity(request);
        return ResponseEntity.ok(cityResponse);
    }
}