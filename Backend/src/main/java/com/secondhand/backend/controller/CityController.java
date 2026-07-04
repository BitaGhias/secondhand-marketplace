package com.secondhand.backend.controller;

import com.secondhand.backend.entity.City;
import com.secondhand.backend.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/cities")
public class CityController {

    @Autowired
    public CityService cityService;

    @GetMapping
    public ResponseEntity<List<City>> getAllCities() {
        return ResponseEntity.ok(cityService.getAllCities());
    }

    @PostMapping("/add")
    public ResponseEntity<?> addCity(@RequestParam String name) {
        try {
            City city = cityService.addCity(name);
            return ResponseEntity.ok(city);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}