package com.secondhand.frontend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.model.User;
import com.secondhand.frontend.util.ApiClient;
import com.secondhand.frontend.util.SessionStore;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class AuthService {

    private static final ObjectMapper objectMapper = ApiClient.getMapper();

    /**
     * ثبت‌نام کاربر به صورت کاملاً ناهمگام (Async)
     */
    public static CompletableFuture<String> register(String fullName, String username, String email, String phone, String password, String confirmPassword) {
        CompletableFuture<String> future = new CompletableFuture<>();
        try {
            RegisterRequest requestObj = new RegisterRequest(fullName, username, password, confirmPassword, phone, email);
            String json = objectMapper.writeValueAsString(requestObj);


            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ApiClient.getBaseUrl() + "/auth/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            ApiClient.getClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        int statusCode = response.statusCode();
                        String responseBody = response.body();

                        if (statusCode == 200 || statusCode == 201) {
                            future.complete(responseBody);
                        } else {
                            future.completeExceptionally(new RuntimeException(responseBody));
                        }
                    })
                    .exceptionally(ex -> {
                        future.completeExceptionally(new RuntimeException("عدم ارتباط با سرور: " + ex.getMessage()));
                        return null;
                    });

        } catch (Exception e) {
            future.completeExceptionally(new RuntimeException("خطا در پردازش اطلاعات: " + e.getMessage()));
        }
        return future;
    }

    /**
     * ورود کاربر به صورت ناهمگام (Async).
     * پس از ورود موفق، نشست (توکن + کاربر) برای اجراهای بعدی برنامه ذخیره می‌شود.
     */
    public static CompletableFuture<LoginResponse> login(String username, String password) {
        CompletableFuture<LoginResponse> future = new CompletableFuture<>();
        try {
            LoginRequest requestObj = new LoginRequest(username, password);
            String json = objectMapper.writeValueAsString(requestObj);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ApiClient.getBaseUrl() + "/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            ApiClient.getClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        try {
                            if (response.statusCode() == 200) {
                                LoginResponse loginResponse = objectMapper.readValue(response.body(), LoginResponse.class);
                                ApiClient.setToken(loginResponse.getToken());
                                // ذخیره نشست برای ورود خودکار در اجراهای بعدی
                                SessionStore.save(loginResponse.getToken(), loginResponse.getUser());
                                future.complete(loginResponse);
                            } else {
                                future.completeExceptionally(new RuntimeException(response.body()));
                            }
                        } catch (Exception e) {
                            future.completeExceptionally(new RuntimeException("خطا در تحلیل پاسخ سرور"));
                        }
                    })
                    .exceptionally(ex -> {
                        future.completeExceptionally(new RuntimeException("عدم ارتباط با سرور: " + ex.getMessage()));
                        return null;
                    });

        } catch (Exception e) {
            future.completeExceptionally(new RuntimeException("خطا در پردازش اطلاعات: " + e.getMessage()));
        }
        return future;
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
        SessionStore.clear();
    }

    // بررسی احراز هویت
    public static boolean isAuthenticated() {
        return ApiClient.isAuthenticated();
    }

    // ===== کلاس‌های داخلی DTO =====
    public static class RegisterRequest {
        public String fullName, username, password, confirmPassword, phoneNumber, email;
        public RegisterRequest(String fullName, String username, String password, String confirmPassword, String phoneNumber, String email) {
            this.fullName = fullName;
            this.username = username;
            this.password = password;
            this.confirmPassword = confirmPassword;
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
