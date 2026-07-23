package com.secondhand.frontend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.model.Item;
import com.secondhand.frontend.util.ApiClient;

import java.io.File;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Client-side service for "item" operations against the backend API.
 * <p>
 * This class is the client-to-server communication layer; it sends requests to the backend API through {@code ApiClient} and converts JSON responses into Java models with Jackson. On a non-successful response the server error message is propagated as an exception.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class ItemService {
    private static final ObjectMapper objectMapper = ApiClient.getMapper();

    // ================= Async =================

    /**
     * Gets active items async.
     *
     * @return a {@code CompletableFuture} that completes asynchronously with the result
     */
    public static CompletableFuture<List<Item>> getActiveItemsAsync() {
        return fetchItemListAsync("/items/approved", "خطا در دریافت آگهی‌ها");
    }

    /**
     * Searches items async.
     *
     * @param keyword the search keyword
     * @param categoryId id of the category
     * @param cityId id of the city
     * @param minPrice minimum price bound
     * @param maxPrice maximum price bound
     * @param sortBy the sort order
     * @return a {@code CompletableFuture} that completes asynchronously with the result
     */
    public static CompletableFuture<List<Item>> searchItemsAsync(String keyword, Long categoryId, Long cityId, Long minPrice, Long maxPrice, String sortBy) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("keyword", keyword == null ? "" : keyword);
                body.put("categoryId", categoryId);
                body.put("cityId", cityId);
                body.put("minPrice", minPrice);
                body.put("maxPrice", maxPrice);
                body.put("sortBy", (sortBy == null || sortBy.isBlank()) ? "newest" : sortBy);
                body.put("searchInDescription", true);
                HttpResponse<String> res = ApiClient.post("/items/search/advanced", body);
                ensureSuccess(res, "خطا در جست‌وجوی آگهی‌ها");
                return objectMapper.readValue(res.body(), new TypeReference<List<Item>>() {});
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    /**
     * Gets pending items async.
     *
     * @return a {@code CompletableFuture} that completes asynchronously with the result
     */
    public static CompletableFuture<List<Item>> getPendingItemsAsync() {
        return fetchItemListAsync("/items/pending", "خطا در دریافت آگهی‌های در انتظار");
    }

    /**
     * Gets user items for admin async.
     *
     * @param userId id of the user
     * @return a {@code CompletableFuture} that completes asynchronously with the result
     */
    public static CompletableFuture<List<Item>> getUserItemsForAdminAsync(Long userId) {
        return fetchItemListAsync("/items/admin/user/" + userId, "خطا در دریافت آگهی‌های کاربر");
    }

    /**
     * Gets my items async.
     *
     * @return a {@code CompletableFuture} that completes asynchronously with the result
     */
    public static CompletableFuture<List<Item>> getMyItemsAsync() {
        return fetchItemListAsync("/items/user", "خطا در دریافت آگهی‌های من");
    }

    /**
     * Gets purchased items async.
     *
     * @return a {@code CompletableFuture} that completes asynchronously with the result
     */
    public static CompletableFuture<List<Item>> getPurchasedItemsAsync() {
        return fetchItemListAsync("/items/purchased", "خطا در دریافت لیست خریدها");
    }

    /**
     * Gets item by id async.
     *
     * @param id unique identifier of the record
     * @return a {@code CompletableFuture} that completes asynchronously with the result
     */
    public static CompletableFuture<Item> getItemByIdAsync(Long id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpResponse<String> res = ApiClient.get("/items/" + id);
                ensureSuccess(res, "خطا در دریافت آگهی");
                return objectMapper.readValue(res.body(), Item.class);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    /**
     * Approves item async.
     *
     * @param id unique identifier of the record
     * @return a {@code CompletableFuture} that completes asynchronously with the result
     */
    public static CompletableFuture<Void> approveItemAsync(Long id) {
        return runAsync(() -> {
            HttpResponse<String> res = ApiClient.put("/items/" + id + "/status?status=APPROVED", null);
            ensureSuccess(res, "خطا در تایید آگهی");
        });
    }

    /**
     * Rejects item async.
     *
     * @param id unique identifier of the record
     * @param reason the rejection reason
     * @return a {@code CompletableFuture} that completes asynchronously with the result
     */
    public static CompletableFuture<Void> rejectItemAsync(Long id, String reason) {
        return runAsync(() -> {
            String encoded = URLEncoder.encode(reason == null ? "" : reason, StandardCharsets.UTF_8);
            HttpResponse<String> res = ApiClient.put("/items/" + id + "/status?status=REJECTED&rejectionReason=" + encoded, null);
            ensureSuccess(res, "خطا در رد آگهی");
        });
    }

    /**
     * Purchases item async.
     *
     * @param itemId id of the ad (item)
     * @return a {@code CompletableFuture} that completes asynchronously with the result
     */
    public static CompletableFuture<Void> purchaseItemAsync(Long itemId) {
        return runAsync(() -> {
            HttpResponse<String> res = ApiClient.put("/items/" + itemId + "/purchase", null);
            ensureSuccess(res, "خطا در خرید کالا");
        });
    }

    /**
     * Deletes item async.
     *
     * @param itemId id of the ad (item)
     * @return a {@code CompletableFuture} that completes asynchronously with the result
     */
    public static CompletableFuture<Void> deleteItemAsync(Long itemId) {
        return runAsync(() -> {
            HttpResponse<String> res = ApiClient.delete("/items/" + itemId);
            ensureSuccess(res, "خطا در حذف آگهی");
        });
    }

    /**
     * Marks as sold async.
     *
     * @param itemId id of the ad (item)
     * @return a {@code CompletableFuture} that completes asynchronously with the result
     */
    public static CompletableFuture<Void> markAsSoldAsync(Long itemId) {
        return runAsync(() -> {
            HttpResponse<String> res = ApiClient.put("/items/" + itemId + "/sold", null);
            ensureSuccess(res, "خطا در تغییر وضعیت آگهی");
        });
    }

    // ================= Sync wrappers =================

    public static List<Item> getActiveItems() throws Exception { return joinUnwrapped(getActiveItemsAsync()); }
    /**
     * Searches items.
     *
     * @param q the "q" value of type {@code String}
     * @param cat the "cat" value of type {@code Long}
     * @param city the city object
     * @param min the "min" value of type {@code Long}
     * @param max the "max" value of type {@code Long}
     * @param sortBy the sort order
     * @return a {@code List<Item>} with the results; empty if nothing matches
     * @throws Exception if the request fails or the server cannot be reached
     */
    public static List<Item> searchItems(String q, Long cat, Long city, Long min, Long max, String sortBy) throws Exception { return joinUnwrapped(searchItemsAsync(q, cat, city, min, max, sortBy)); }
    public static List<Item> getPendingItems() throws Exception { return joinUnwrapped(getPendingItemsAsync()); }
    public static List<Item> getUserItemsForAdmin(Long userId) throws Exception { return joinUnwrapped(getUserItemsForAdminAsync(userId)); }
    public static List<Item> getMyItems() throws Exception { return joinUnwrapped(getMyItemsAsync()); }
    public static Item getItemById(Long id) throws Exception { return joinUnwrapped(getItemByIdAsync(id)); }
    public static void approveItem(Long id) throws Exception { joinUnwrapped(approveItemAsync(id)); }
    public static void rejectItem(Long id, String reason) throws Exception { joinUnwrapped(rejectItemAsync(id, reason)); }
    public static void deleteItem(Long id) throws Exception { joinUnwrapped(deleteItemAsync(id)); }

    // ================= ثبت و ویرایش =================

    public static Item createItem(ItemCreateRequest request) throws Exception {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("title", request.title);
        fields.put("description", request.description);
        fields.put("price", String.valueOf(request.price));
        fields.put("categoryId", String.valueOf(request.categoryId));
        fields.put("cityId", String.valueOf(request.cityId));

        List<File> files = new ArrayList<>();
        if (request.imagePaths != null) {
            for (String path : request.imagePaths) {
                if (path != null && !path.isBlank()) files.add(new File(path));
            }
        }

        HttpResponse<String> res = ApiClient.postMultipart("/items/create", fields, "images", files);
        ensureSuccess(res, "خطا در ثبت آگهی");
        return objectMapper.readValue(res.body(), Item.class);
    }

    /**
     * Updates item.
     *
     * @param id unique identifier of the record
     * @param request request body received from the client
     * @return the resulting {@code Item} instance
     * @throws Exception if the request fails or the server cannot be reached
     */
    public static Item updateItem(Long id, ItemUpdateRequest request) throws Exception {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("title", request.title);
        fields.put("description", request.description);
        fields.put("price", String.valueOf(request.price));
        fields.put("categoryId", String.valueOf(request.categoryId));
        fields.put("cityId", String.valueOf(request.cityId));

        List<File> files = new ArrayList<>();
        if (request.newImagePaths != null) {
            for (String path : request.newImagePaths) {
                if (path != null && !path.isBlank()) files.add(new File(path));
            }
        }

        HttpResponse<String> res = ApiClient.putMultipart("/items/" + id, fields, request.removedImageIds, "images", files);
        ensureSuccess(res, "خطا در ویرایش آگهی");
        return objectMapper.readValue(res.body(), Item.class);
    }

    // ================= کمکی =================

    /**
     * Fetches item list async.
     *
     * @param endpoint API path relative to the base URL
     * @param errorPrefix prefix used in error messages
     * @return a {@code CompletableFuture} that completes asynchronously with the result
     */
    private static CompletableFuture<List<Item>> fetchItemListAsync(String endpoint, String errorPrefix) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpResponse<String> res = ApiClient.get(endpoint);
                ensureSuccess(res, errorPrefix);
                return objectMapper.readValue(res.body(), new TypeReference<List<Item>>() {});
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    /**
     * Functional interface similar to {@code Runnable} whose body may throw a checked {@code Exception}.
     */
    private interface ThrowingRunnable { void run() throws Exception; }

    /**
     * Runs async.
     *
     * @param action the operation to execute
     * @return a {@code CompletableFuture} that completes asynchronously with the result
     */
    private static CompletableFuture<Void> runAsync(ThrowingRunnable action) {
        return CompletableFuture.runAsync(() -> {
            try {
                action.run();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    /**
     * Joins unwrapped.
     *
     * @param future the future to wait for
     * @return the resulting {@code <T> T} instance
     * @throws Exception if the request fails or the server cannot be reached
     */
    private static <T> T joinUnwrapped(CompletableFuture<T> future) throws Exception {
        try {
            return future.join();
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

    // ================= کلاس‌های درخواست =================

    /**
     * Request payload used when creating a new ad.
     */
    public static class ItemCreateRequest {
        public String title, description;
        public Long price, categoryId, cityId;
        public List<String> imagePaths;

        /**
         * Performs the "item create request" operation.
         *
         * @param t the "t" value of type {@code String}
         * @param d the "d" value of type {@code String}
         * @param p the "p" value of type {@code Long}
         * @param cat the "cat" value of type {@code Long}
         * @param city the city object
         * @param imgs the "imgs" value of type {@code List<String>}
         */
        public ItemCreateRequest(String t, String d, Long p, Long cat, Long city, List<String> imgs) {
            this.title = t; this.description = d; this.price = p;
            this.categoryId = cat; this.cityId = city; this.imagePaths = imgs;
        }
    }

    /**
     * Request payload used when editing an existing ad.
     */
    public static class ItemUpdateRequest {
        public String title, description;
        public Long price, categoryId, cityId;
        public List<Long> removedImageIds;
        public List<String> newImagePaths;

        /**
         * Performs the "item update request" operation.
         *
         * @param t the "t" value of type {@code String}
         * @param d the "d" value of type {@code String}
         * @param p the "p" value of type {@code Long}
         * @param cat the "cat" value of type {@code Long}
         * @param city the city object
         */
        public ItemUpdateRequest(String t, String d, Long p, Long cat, Long city) {
            this.title = t; this.description = d; this.price = p;
            this.categoryId = cat; this.cityId = city;
        }
    }
}