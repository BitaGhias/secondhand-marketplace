package com.secondhand.backend.dto;

import com.secondhand.backend.constant.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String fullName;
    private String username;
    private Role role;
    private boolean blocked;
}
