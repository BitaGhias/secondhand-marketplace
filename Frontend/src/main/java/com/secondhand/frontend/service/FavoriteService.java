package com.secondhand.frontend.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.model.Item;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class FavoriteService {

    private static final ObjectMapper objectMapper = ApiClient.getMapper();

    // دریافت لیست علاقه‌مندی‌ها
    // بک‌اند لیست FavoriteResponse برمی‌گرداند (نه Item)؛ برای نمایش کارت‌ها،
    // جزئیات کامل هر آگهی را جداگانه می‌گیریم و در صورت خطا به داده حداقلی بسنده می‌کنیم
    public static List<Item> getFavorites() throws Exception {
        HttpResponse<String> response = ApiClient.get("/favorites/user");

        if (response.statusCode() != 200) {
            throw new Exception("خطا در دریافت علاقه‌مندی‌ها: " + response.body());
        }

        List<FavoriteEntry> favorites = objectMapper.readValue(
                response.body(), new TypeReference<List<FavoriteEntry>>() {});

        List<Item> items = new ArrayList<>();
        for (FavoriteEntry fav : favorites) {
            if (fav.itemId == null) continue;
            try {
                items.add(ItemService.getItemById(fav.itemId));
            } catch (Exception e) {
                // اگر جزئیات کامل در دسترس نبود، از داده‌های خود FavoriteResponse استفاده کن
                Item item = new Item();
                item.setId(fav.itemId);
                item.setTitle(fav.itemTitle);
                item.setPrice(fav.itemPrice != null ? fav.itemPrice : 0);
                item.setStatus(fav.itemStatus);
                items.add(item);
            }
        }
        return items;
    }

    // افزودن به علاقه‌مندی‌ها
    public static void addFavorite(Long itemId) throws Exception {
        FavoriteRequest request = new FavoriteRequest(itemId);
        HttpResponse<String> response = ApiClient.post("/favorites/add", request);

        if (response.statusCode() != 200 && response.statusCode() != 201) {
            throw new Exception("خطا در افزودن به علاقه‌مندی‌ها: " + response.body());
        }
    }

    // حذف از علاقه‌مندی‌ها
    public static void removeFavorite(Long itemId) throws Exception {
        FavoriteRequest request = new FavoriteRequest(itemId);
        HttpResponse<String> response = ApiClient.delete("/favorites/remove", request);

        if (response.statusCode() != 200 && response.statusCode() != 204) {
            throw new Exception("خطا در حذف از علاقه‌مندی‌ها: " + response.body());
        }
    }

    // بررسی اینکه آیا آگهی در علاقه‌مندی‌ها هست
    public static boolean isFavorite(Long itemId) throws Exception {
        HttpResponse<String> response = ApiClient.get("/favorites/check?itemId=" + itemId);
        return response.statusCode() == 200 && Boolean.parseBoolean(response.body().trim());
    }

    public static class FavoriteRequest {
        public Long itemId;
        public FavoriteRequest(Long itemId) {
            this.itemId = itemId;
        }
    }

    // مطابق FavoriteResponse بک‌اند
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FavoriteEntry {
        public Long id;
        public Long itemId;
        public String itemTitle;
        public Double itemPrice;
        public String itemStatus;
        public Long userId;
    }
}
