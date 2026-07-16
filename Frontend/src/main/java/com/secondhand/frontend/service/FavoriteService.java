package com.secondhand.frontend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.model.Item;
import com.secondhand.frontend.service.ApiClient;

import java.net.http.HttpResponse;
import java.util.List;

public class FavoriteService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // دریافت لیست علاقه‌مندی‌ها
    public static List<Item> getFavorites() throws Exception {
        // ✅ مسیر درست: /api/favorites/user
        HttpResponse<String> response = ApiClient.get("/favorites/user");

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Item>>() {});
        } else {
            throw new Exception("خطا در دریافت علاقه‌مندی‌ها: " + response.body());
        }
    }

    // افزودن به علاقه‌مندی‌ها
    public static void addFavorite(Long itemId) throws Exception {
        FavoriteRequest request = new FavoriteRequest(itemId);
        // ✅ مسیر درست: /api/favorites/add
        HttpResponse<String> response = ApiClient.post("/favorites/add", request);

        if (response.statusCode() != 200 && response.statusCode() != 201) {
            throw new Exception("خطا در افزودن به علاقه‌مندی‌ها: " + response.body());
        }
    }

    // حذف از علاقه‌مندی‌ها
    public static void removeFavorite(Long itemId) throws Exception {
        FavoriteRequest request = new FavoriteRequest(itemId);
        // ✅ مسیر درست: /api/favorites/remove
        HttpResponse<String> response = ApiClient.delete("/favorites/remove", request);

        if (response.statusCode() != 200 && response.statusCode() != 204) {
            throw new Exception("خطا در حذف از علاقه‌مندی‌ها: " + response.body());
        }
    }

    // بررسی اینکه آیا آگهی در علاقه‌مندی‌ها هست
    public static boolean isFavorite(Long itemId) throws Exception {
        // ✅ مسیر درست: /api/favorites/check?itemId=...
        HttpResponse<String> response = ApiClient.get("/favorites/check?itemId=" + itemId);
        return response.statusCode() == 200 && Boolean.parseBoolean(response.body());
    }

    public static class FavoriteRequest {
        public Long itemId;
        public FavoriteRequest(Long itemId) {
            this.itemId = itemId;
        }
    }
}