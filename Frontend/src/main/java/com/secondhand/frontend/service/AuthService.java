package com.secondhand.frontend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.model.User;
import com.secondhand.frontend.service.ApiClient;

import java.net.http.HttpResponse;

public class AuthService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // ثبت‌نام
    public static User register(String fullName, String username, String password,
                                String phoneNumber, String email) throws Exception {
        RegisterRequest request = new RegisterRequest(fullName, username, password, phoneNumber, email);
        HttpResponse<String> response = ApiClient.post("/auth/register", request);

        if (response.statusCode() == 201) {
            return objectMapper.readValue(response.body(), User.class);
        } else {
            throw new Exception("خطا در ثبت‌نام: " + response.body());
        }
    }

    // ورود
    public static LoginResponse login(String username, String password) throws Exception {
        LoginRequest request = new LoginRequest(username, password);
        HttpResponse<String> response = ApiClient.post("/auth/login", request);

        if (response.statusCode() == 200) {
            LoginResponse loginResponse = objectMapper.readValue(response.body(), LoginResponse.class);
            ApiClient.setToken(loginResponse.getToken());
            return loginResponse;
        } else {
            throw new Exception("خطا در ورود: " + response.body());
        }
    }

    // دریافت پروفایل
    public static User getProfile() throws Exception {
        HttpResponse<String> response = ApiClient.get("/auth/profile");

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), User.class);
        } else {
            throw new Exception("خطا در دریافت پروفایل: " + response.body());
        }
    }

    // خروج
    public static void logout() {
        ApiClient.clearToken();
    }

    // بررسی احراز هویت
    public static boolean isAuthenticated() {
        return ApiClient.isAuthenticated();
    }

    // کلاس‌های داخلی برای Request
    public static class RegisterRequest {
        public String fullName, username, password, phoneNumber, email;
        public RegisterRequest(String fullName, String username, String password,
                               String phoneNumber, String email) {
            this.fullName = fullName;
            this.username = username;
            this.password = password;
            this.phoneNumber = phoneNumber;
            this.email = email;
        }
    }

    public static class LoginRequest {
        public String username, password;
        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    public static class LoginResponse {
        private User user;
        private String token;

        public User getUser() { return user; }
        public void setUser(User user) { this.user = user; }
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }
}