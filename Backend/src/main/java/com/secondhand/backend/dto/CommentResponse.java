package com.secondhand.backend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private Long id;
    private String text;
    private Long itemId;
    private String itemTitle;
    private Long userId;
    private String username;
}