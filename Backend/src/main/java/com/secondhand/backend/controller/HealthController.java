package com.secondhand.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * REST controller exposing the "health" API endpoints.
 * <p>
 * This class is the entry point for HTTP requests; it delegates the work to the service layer and returns the result as JSON with a proper status code. Errors are handled centrally by {@code GlobalExceptionHandler}.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
@RestController
public class HealthController {

    /**
     * Checks health.
     *
     * @return an HTTP response containing the operation result and a proper status code
     */
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