package com.secondhand.frontend.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.model.Item;
import com.secondhand.frontend.util.ApiClient;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Client-side service for "favorite" operations against the backend API.
 * <p>
 * This class is the client-to-server communication layer; it sends requests to the backend API through {@code ApiClient} and converts JSON responses into Java models with Jackson. On a non-successful response the server error message is propagated as an exception.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class FavoriteService {

    private static final ObjectMapper objectMapper = ApiClient.getMapper();

    // دریافت لیست علاقه‌مندی‌ها
    /**
     * Gets favorites.
     *
     * @return a {@code List<Item>} with the results; empty if nothing matches
     * @throws Exception if the request fails or the server cannot be reached
     */
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
                // در صورت بروز خطا، از داده‌های داخل FavoriteResponse به عنوان داده‌های حداقل استفاده می‌شود
                Item item = new Item();
                item.setId(fav.itemId);
                item.setTitle(fav.itemTitle);
                // 🟢 قیمت کماکان سازگار با Long مقداردهی می‌شود
                item.setPrice(fav.itemPrice != null ? fav.itemPrice : 0L);
                item.setStatus(fav.itemStatus);
                items.add(item);
            }
        }
        return items;
    }

    // افزودن به علاقه‌مندی‌ها
    /**
     * Adds favorite.
     *
     * @param itemId id of the ad (item)
     * @throws Exception if the request fails or the server cannot be reached
     */
    public static void addFavorite(Long itemId) throws Exception {
        FavoriteRequest request = new FavoriteRequest(itemId);
        HttpResponse<String> response = ApiClient.post("/favorites/add", request);

        if (response.statusCode() != 200 && response.statusCode() != 201) {
            throw new Exception("خطا در افزودن به علاقه‌مندی‌ها: " + response.body());
        }
    }

    // حذف از علاقه‌مندی‌ها (مطابق بک‌اند: DELETE /favorites/remove?itemId=X با @RequestParam)
    /**
     * Removes favorite.
     *
     * @param itemId id of the ad (item)
     * @throws Exception if the request fails or the server cannot be reached
     */
    public static void removeFavorite(Long itemId) throws Exception {
        HttpResponse<String> response = ApiClient.delete("/favorites/remove?itemId=" + itemId);

        if (response.statusCode() != 200 && response.statusCode() != 204) {
            throw new Exception("خطا در حذف از علاقه‌مندی‌ها: " + response.body());
        }
    }

    // بررسی اینکه آیا آگهی در علاقه‌مندی‌ها هست
    /**
     * Checks whether the "favorite" condition holds.
     *
     * @param itemId id of the ad (item)
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     * @throws Exception if the request fails or the server cannot be reached
     */
    public static boolean isFavorite(Long itemId) throws Exception {
        HttpResponse<String> response = ApiClient.get("/favorites/check?itemId=" + itemId);
        return response.statusCode() == 200 && Boolean.parseBoolean(response.body().trim());
    }

    /**
     * Nested class used by {@code FavoriteService}.
     */
    public static class FavoriteRequest {
        public Long itemId;
        /**
         * Performs the "favorite request" operation.
         *
         * @param itemId id of the ad (item)
         */
        public FavoriteRequest(Long itemId) {
            this.itemId = itemId;
        }
    }

    // مطابق FavoriteResponse بک‌اند
    /**
     * Nested class used by {@code FavoriteService}.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FavoriteEntry {
        public Long id;
        public Long itemId;
        public String itemTitle;
        public Long itemPrice;
        public String itemStatus;
        public Long userId;
    }
}