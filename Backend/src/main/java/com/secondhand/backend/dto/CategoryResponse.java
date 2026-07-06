package com.secondhand.backend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
}