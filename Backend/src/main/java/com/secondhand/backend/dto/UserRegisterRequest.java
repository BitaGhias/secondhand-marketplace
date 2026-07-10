package com.secondhand.backend.dto;

import lombok.Data;

@Data
public class UserRegisterRequest {
    private String fullName;
    private String username;
    private String password;
    private String phoneNumber;
}
