package com.secondhand.frontend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.model.User;
import com.secondhand.frontend.util.ApiClient;

import java.io.File;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Client-side service for "user" operations against the backend API.
 * <p>
 * This class is the client-to-server communication layer; it sends requests to the backend API through {@code ApiClient} and converts JSON responses into Java models with Jackson. On a non-successful response the server error message is propagated as an exception.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class UserService {
    private static final ObjectMapper objectMapper = ApiClient.getMapper();

    /**
     * Gets current user.
     *
     * @return the resulting {@code User} instance
     * @throws Exception if the request fails or the server cannot be reached
     */
    public static User getCurrentUser() throws Exception {
        HttpResponse<String> response = ApiClient.get("/auth/profile");
        ensureSuccess(response, "خطا در دریافت پروفایل");
        return objectMapper.readValue(response.body(), User.class);
    }

    /**
     * Gets current user async.
     *
     * @return a {@code CompletableFuture} that completes asynchronously with the result
     */
    public static CompletableFuture<User> getCurrentUserAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getCurrentUser();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    /**
     * Updates profile async.
     *
     * @param fullName the "full name" value of type {@code String}
     * @param phone the "phone" value of type {@code String}
     * @param email the email address
     * @return a {@code CompletableFuture} that completes asynchronously with the result
     */
    public static CompletableFuture<User> updateProfileAsync(String fullName, String phone, String email) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, String> body = new LinkedHashMap<>();
                body.put("fullName", fullName);
                body.put("phoneNumber", phone);
                body.put("email", email);
                HttpResponse<String> res = ApiClient.put("/auth/profile", body);
                ensureSuccess(res, "خطا در به‌روزرسانی پروفایل");
                return objectMapper.readValue(res.body(), User.class);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    /**
     * Changes password async.
     *
     * @param oldPass the "old pass" value of type {@code String}
     * @param newPass the "new pass" value of type {@code String}
     * @return a {@code CompletableFuture} that completes asynchronously with the result
     */
    public static CompletableFuture<Void> changePasswordAsync(String oldPass, String newPass) {
        return CompletableFuture.runAsync(() -> {
            try {
                Map<String, String> body = new LinkedHashMap<>();
                body.put("oldPassword", oldPass);
                body.put("newPassword", newPass);
                HttpResponse<String> res = ApiClient.put("/auth/change-password", body);
                ensureSuccess(res, "خطا در تغییر رمز عبور");
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    /**
     * Uploads profile image async.
     *
     * @param file the input file
     * @return a {@code CompletableFuture} that completes asynchronously with the result
     */
    public static CompletableFuture<User> uploadProfileImageAsync(File file) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpResponse<String> res = ApiClient.postMultipart(
                        "/auth/profile/image", Map.of(), "image", List.of(file));
                ensureSuccess(res, "خطا در آپلود عکس پروفایل");
                return objectMapper.readValue(res.body(), User.class);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    /**
     * Gets all users async.
     *
     * @return a {@code CompletableFuture} that completes asynchronously with the result
     */
    public static CompletableFuture<List<User>> getAllUsersAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpResponse<String> res = ApiClient.get("/auth/admin/all");
                ensureSuccess(res, "خطا در دریافت کاربران");
                return objectMapper.readValue(res.body(), new TypeReference<List<User>>() {});
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    /**
     * Gets all users.
     *
     * @return a {@code List<User>} with the results; empty if nothing matches
     * @throws Exception if the request fails or the server cannot be reached
     */
    public static List<User> getAllUsers() throws Exception {
        try {
            return getAllUsersAsync().join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof Exception ex) throw ex;
            throw e;
        }
    }

    /**
     * Toggles block async.
     *
     * @param userId id of the user
     * @param block the "block" value of type {@code boolean}
     * @return a {@code CompletableFuture} that completes asynchronously with the result
     */
    public static CompletableFuture<User> toggleBlockAsync(Long userId, boolean block) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpResponse<String> res = ApiClient.post(
                        "/auth/admin/toggle-block?userId=" + userId + "&block=" + block);
                ensureSuccess(res, "خطا در تغییر وضعیت کاربر");
                return objectMapper.readValue(res.body(), User.class);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    /**
     * Toggles block.
     *
     * @param userId id of the user
     * @param block the "block" value of type {@code boolean}
     * @return the resulting {@code User} instance
     * @throws Exception if the request fails or the server cannot be reached
     */
    public static User toggleBlock(Long userId, boolean block) throws Exception {
        try {
            return toggleBlockAsync(userId, block).join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof Exception ex) throw ex;
            throw e;
        }
    }

    /**
     * Ensures success.
     *
     * @param res the HTTP response
     * @param prefix prefix used in error messages
     * @throws Exception if the request fails or the server cannot be reached
     */
    private static void ensureSuccess(HttpResponse<String> res, String prefix) throws Exception {
        try {
            ApiClient.ensureSuccess(res);
        } catch (Exception ex) {
            throw new Exception(prefix + ": " + ex.getMessage(), ex);
        }
    }
}