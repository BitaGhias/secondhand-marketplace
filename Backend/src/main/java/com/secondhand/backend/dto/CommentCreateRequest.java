package com.secondhand.backend.dto;

import lombok.Data;

@Data
public class CommentCreateRequest {
    private Long itemId;
    private String text;
}