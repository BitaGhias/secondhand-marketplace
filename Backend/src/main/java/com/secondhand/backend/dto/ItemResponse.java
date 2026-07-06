package com.secondhand.backend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemResponse {
    private Long id;
    private String title;
    private String description;
    private double price;
    private String status; // APPROVED, PENDING, SOLD
    private String categoryName;
    private String cityName;
    private String ownerUsername;
    private Long ownerId;
}