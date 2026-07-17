package com.secondhand.frontend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.model.User;
import com.secondhand.frontend.service.ApiClient;

import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // ============================================
    // 👤 عملیات کاربر عادی
    // ============================================

    /**
     * دریافت اطلاعات کاربر جاری (از توکن)
     */
    public static User getCurrentUser() throws Exception {
        HttpResponse<String> response = ApiClient.get("/auth/profile");

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), User.class);
        } else {
            throw new Exception("خطا در دریافت اطلاعات کاربر: " + response.body());
        }
    }

    /**
     * به‌روزرسانی پروفایل کاربر
     */
    public static User updateProfile(String fullName, String phoneNumber, String email) throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("fullName", fullName);
        body.put("phoneNumber", phoneNumber);
        body.put("email", email);

        HttpResponse<String> response = ApiClient.put("/auth/profile", body);

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), User.class);
        } else {
            throw new Exception("خطا در به‌روزرسانی پروفایل: " + response.body());
        }
    }

    /**
     * تغییر رمز عبور
     */
    public static void changePassword(String oldPassword, String newPassword) throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("oldPassword", oldPassword);
        body.put("newPassword", newPassword);

        HttpResponse<String> response = ApiClient.put("/auth/change-password", body);

        if (response.statusCode() != 200) {
            throw new Exception("خطا در تغییر رمز عبور: " + response.body());
        }
    }

    // ============================================
    // 🛡️ عملیات ادمین (مدیریت کاربران)
    // ============================================

    /**
     * دریافت لیست همه کاربران (فقط ادمین)
     */
    public static List<User> getAllUsers() throws Exception {
        HttpResponse<String> response = ApiClient.get("/auth/admin/all");

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<User>>() {});
        } else {
            throw new Exception("خطا در دریافت کاربران: " + response.body());
        }
    }

    /**
     * مسدود کردن یا فعال‌سازی کاربر (فقط ادمین)
     * @param userId شناسه کاربر
     * @param block true = مسدود, false = فعال
     */
    public static User toggleBlock(Long userId, boolean block) throws Exception {
        Map<String, Boolean> body = new HashMap<>();
        body.put("blocked", block);

        HttpResponse<String> response = ApiClient.post("/auth/admin/toggle-block?userId=" + userId + "&block=" + block, null);

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), User.class);
        } else {
            throw new Exception("خطا در تغییر وضعیت کاربر: " + response.body());
        }
    }

    /**
     * ارتقا کاربر به ادمین (فقط ادمین)
     */
    public static User makeAdmin(Long userId) throws Exception {
        HttpResponse<String> response = ApiClient.post("/auth/admin/make-admin?userId=" + userId, null);

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), User.class);
        } else {
            throw new Exception("خطا در ارتقا کاربر: " + response.body());
        }
    }

    /**
     * بررسی اینکه کاربر جاری ادمین است یا نه
     */
    public static boolean isAdmin() throws Exception {
        HttpResponse<String> response = ApiClient.get("/auth/admin/is-admin");

        if (response.statusCode() == 200) {
            return Boolean.parseBoolean(response.body());
        } else {
            return false;
        }
    }

    // ============================================
    // 🔍 متدهای کمکی
    // ============================================

    /**
     * جستجوی کاربران بر اساس نام یا نام کاربری (فقط ادمین)
     */
    public static List<User> searchUsers(String keyword) throws Exception {
        HttpResponse<String> response = ApiClient.get("/auth/admin/search?keyword=" + keyword);

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<User>>() {});
        } else {
            throw new Exception("خطا در جستجوی کاربران: " + response.body());
        }
    }

    /**
     * دریافت آمار کاربران (فقط ادمین)
     */
    public static Map<String, Long> getUserStats() throws Exception {
        HttpResponse<String> response = ApiClient.get("/auth/admin/stats");

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<Map<String, Long>>() {});
        } else {
            throw new Exception("خطا در دریافت آمار کاربران: " + response.body());
        }
    }

    /**
     * دریافت کاربران مسدود شده (فقط ادمین)
     */
    public static List<User> getBlockedUsers() throws Exception {
        HttpResponse<String> response = ApiClient.get("/auth/admin/blocked");

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<User>>() {});
        } else {
            throw new Exception("خطا در دریافت کاربران مسدود: " + response.body());
        }
    }
}