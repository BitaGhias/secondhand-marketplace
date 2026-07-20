package com.secondhand.frontend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.model.User;
import com.secondhand.frontend.util.ApiClient;

import java.io.File;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class UserService {
    private static final ObjectMapper objectMapper = ApiClient.getMapper();

    public static User getCurrentUser() throws Exception {
        HttpResponse<String> response = ApiClient.get("/auth/profile");
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), User.class);
        }
        throw new Exception("خطا در دریافت پروفایل: " + response.body());
    }

    public static CompletableFuture<User> getCurrentUserAsync() {
        CompletableFuture<User> future = new CompletableFuture<>();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ApiClient.getBaseUrl() + "/auth/profile"))
                .header("Authorization", "Bearer " + ApiClient.getToken())
                .GET()
                .build();

        ApiClient.getClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    try {
                        if (response.statusCode() == 200) {
                            future.complete(objectMapper.readValue(response.body(), User.class));
                        } else {
                            future.completeExceptionally(new RuntimeException(response.body()));
                        }
                    } catch (Exception e) {
                        future.completeExceptionally(new RuntimeException("خطا در تحلیل داده‌های کاربر"));
                    }
                }).exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    return null;
                });
        return future;
    }

    public static CompletableFuture<User> updateProfileAsync(String fullName, String phone, String email) {
        CompletableFuture<User> future = new CompletableFuture<>();
        try {
            String json = String.format("{\"fullName\":\"%s\",\"phoneNumber\":\"%s\",\"email\":\"%s\"}", fullName, phone, email);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ApiClient.getBaseUrl() + "/user/update"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + ApiClient.getToken())
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            ApiClient.getClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        try {
                            if (response.statusCode() == 200) {
                                future.complete(objectMapper.readValue(response.body(), User.class));
                            } else {
                                future.completeExceptionally(new RuntimeException(response.body()));
                            }
                        } catch (Exception e) {
                            future.completeExceptionally(new RuntimeException("خطا در به‌روزرسانی پروفایل"));
                        }
                    });
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    public static CompletableFuture<Void> changePasswordAsync(String oldPass, String newPass) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            String json = String.format("{\"oldPassword\":\"%s\",\"newPassword\":\"%s\"}", oldPass, newPass);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ApiClient.getBaseUrl() + "/user/change-password"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + ApiClient.getToken())
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            ApiClient.getClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() == 200) {
                            future.complete(null);
                        } else {
                            future.completeExceptionally(new RuntimeException(response.body()));
                        }
                    });
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    public static CompletableFuture<User> uploadProfileImageAsync(File file) {
        CompletableFuture<User> future = new CompletableFuture<>();
        // فرض بر این است که آپلود عکس آواتار به صورت ساده یا Base64/Multipart هندل می‌شود
        // یک پیاده‌سازی موقت Async برای پر نشدن ارور ادیتور:
        future.completeExceptionally(new RuntimeException("متد آپلود مولتی‌پارت نیاز به ساختار فرم دارد."));
        return future;
    }
}