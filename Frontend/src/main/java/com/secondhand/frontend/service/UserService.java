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
 * سرویس کاربران — مسیرها دقیقاً مطابق UserController بک‌اند (/api/auth/**):
 *   GET  /api/auth/profile               پروفایل من
 *   PUT  /api/auth/profile               ویرایش پروفایل (fullName/phoneNumber/email)
 *   PUT  /api/auth/change-password       تغییر رمز (oldPassword/newPassword)
 *   POST /api/auth/profile/image         آپلود عکس پروفایل (multipart، فیلد image)
 *   GET  /api/auth/admin/all             لیست کاربران (ادمین)
 *   POST /api/auth/admin/toggle-block    مسدود/فعال‌سازی (پارامترهای userId و block)
 */
public class UserService {
    private static final ObjectMapper objectMapper = ApiClient.getMapper();

    public static User getCurrentUser() throws Exception {
        HttpResponse<String> response = ApiClient.get("/auth/profile");
        ensureSuccess(response, "خطا در دریافت پروفایل");
        return objectMapper.readValue(response.body(), User.class);
    }

    public static CompletableFuture<User> getCurrentUserAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getCurrentUser();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

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

    /** آپلود عکس پروفایل به صورت multipart/form-data با فیلد "image" */
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

    public static List<User> getAllUsers() throws Exception {
        try {
            return getAllUsersAsync().join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof Exception ex) throw ex;
            throw e;
        }
    }

    /** مسدود/فعال‌سازی کاربر — POST /auth/admin/toggle-block?userId=..&block=.. */
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

    public static User toggleBlock(Long userId, boolean block) throws Exception {
        try {
            return toggleBlockAsync(userId, block).join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof Exception ex) throw ex;
            throw e;
        }
    }

    private static void ensureSuccess(HttpResponse<String> res, String prefix) throws Exception {
        try {
            ApiClient.ensureSuccess(res);
        } catch (Exception ex) {
            throw new Exception(prefix + ": " + ex.getMessage(), ex);
        }
    }
}