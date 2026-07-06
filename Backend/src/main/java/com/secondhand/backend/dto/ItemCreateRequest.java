package com.secondhand.backend.dto;

import lombok.Data;

@Data
public class ItemCreateRequest {
    private String title;
    private String description;
    private double price;
    private Long categoryId;
    private Long cityId;
    private Long userId;
}
