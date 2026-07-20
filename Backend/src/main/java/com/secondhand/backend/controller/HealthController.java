package com.secondhand.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> checkHealth() {
        return ResponseEntity.ok(
                Map.of(
                        "status", "UP",
                        "message", "Backend is running",
                        "timestamp", LocalDateTime.now()
                )
        );
    }
}