package com.secondhand.backend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingResponse {
    private Long id;
    private int score;
    private String comment;
    private Long itemId;
    private String itemTitle;
    private Long raterId;
    private String raterUsername;
    private Long sellerId;
    private String sellerUsername;
}