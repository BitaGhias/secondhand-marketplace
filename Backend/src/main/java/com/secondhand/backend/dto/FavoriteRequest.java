package com.secondhand.backend.dto;

import lombok.Data;

@Data
public class FavoriteRequest {
    private Long userId;
    private Long itemId;
}