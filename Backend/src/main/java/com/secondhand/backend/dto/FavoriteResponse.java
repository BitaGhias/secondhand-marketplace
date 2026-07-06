package com.secondhand.backend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteResponse {
    private Long id;
    private Long itemId;
    private String itemTitle;
    private double itemPrice;
    private String itemStatus;
    private Long userId;
}