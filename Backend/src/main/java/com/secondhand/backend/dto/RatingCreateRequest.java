package com.secondhand.backend.dto;

import lombok.Data;

@Data
public class RatingCreateRequest {
    private Long itemId;
    private Long raterId;
    private int score;
    private String comment;
}