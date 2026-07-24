package com.secondhand.frontend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.model.User;
import com.secondhand.frontend.util.ApiClient;
import com.secondhand.frontend.util.SessionStore;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

/**
 * Client-side service for "auth" operations against the backend API.
 * <p>
 * This class is the client-to-server communication layer; it sends requests to the backend API through {@code ApiClient} and converts JSON responses into Java models with Jackson. On a non-successful response the server error message is propagated as an exception.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class AuthService {

    private static final ObjectMapper objectMapper = ApiClient.getMapper();

    /**
     * Registers.
     *
     * @param fullName the "full name" value of type {@code String}
     * @param username the username
     * @param email the email address
     * @param phone the "phone" value of type {@code String}
     * @param password the password
     * @param confirmPassword the "confirm password" value of type {@code String}
     * @return a {@code CompletableFuture} that completes asynchronously with the result
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
                            future.completeExceptionally(new RuntimeException(
                                    ApiClient.extractErrorMessage(responseBody)
                            ));
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
     * Logs in.
     *
     * @param username the username
     * @param password the password
     * @return a {@code CompletableFuture} that completes asynchronously with the result
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
                                future.completeExceptionally(new RuntimeException(
                                        ApiClient.extractErrorMessage(response.body())
                                ));
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
    /**
     * Gets profile.
     *
     * @return the resulting {@code User} instance
     * @throws Exception if the request fails or the server cannot be reached
     */
    public static User getProfile() throws Exception {
        HttpResponse<String> response = ApiClient.get("/auth/profile");
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), User.class);
        } else {
            throw new Exception(
                    ApiClient.extractErrorMessage(response.body())
            );
        }
    }

    // خروج
    /**
     * Logs out.
     */
    public static void logout() {
        ApiClient.clearToken();
        SessionStore.clear();
    }

    // بررسی احراز هویت
    /**
     * Checks whether the "authenticated" condition holds.
     *
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     */
    public static boolean isAuthenticated() {
        return ApiClient.isAuthenticated();
    }

    // ===== کلاس‌های داخلی DTO =====
    /**
     * Nested class used by {@code AuthService}.
     */
    public static class RegisterRequest {
        public String fullName, username, password, confirmPassword, phoneNumber, email;
        /**
         * Registers request.
         *
         * @param fullName the "full name" value of type {@code String}
         * @param username the username
         * @param password the password
         * @param confirmPassword the "confirm password" value of type {@code String}
         * @param phoneNumber the phone number
         * @param email the email address
         */
        public RegisterRequest(String fullName, String username, String password, String confirmPassword, String phoneNumber, String email) {
            this.fullName = fullName;
            this.username = username;
            this.password = password;
            this.confirmPassword = confirmPassword;
            this.phoneNumber = phoneNumber;
            this.email = email;
        }
    }

    /**
     * Nested class used by {@code AuthService}.
     */
    public static class LoginRequest {
        public String username, password;
        /**
         * Logs in request.
         *
         * @param username the username
         * @param password the password
         */
        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    /**
     * Nested class used by {@code AuthService}.
     */
    public static class LoginResponse {
        private User user;
        private String token;

        public User getUser() { return user; }
        public void setUser(User user) { this.user = user; }
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }
}
