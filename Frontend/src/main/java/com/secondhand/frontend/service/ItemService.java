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

public class ItemService {
    private static final ObjectMapper objectMapper = ApiClient.getMapper();

    // ================= Async =================

    public static CompletableFuture<List<Item>> getActiveItemsAsync() {
        return fetchItemListAsync("/items/approved", "خطا در دریافت آگهی‌ها");
    }

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
                HttpResponse<String> res = ApiClient.post("/items/search/advanced", body);
                ensureSuccess(res, "خطا در جست‌وجوی آگهی‌ها");
                return objectMapper.readValue(res.body(), new TypeReference<List<Item>>() {});
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    public static CompletableFuture<List<Item>> getPendingItemsAsync() {
        return fetchItemListAsync("/items/pending", "خطا در دریافت آگهی‌های در انتظار");
    }

    public static CompletableFuture<List<Item>> getUserItemsForAdminAsync(Long userId) {
        return fetchItemListAsync("/items/admin/user/" + userId, "خطا در دریافت آگهی‌های کاربر");
    }

    public static CompletableFuture<List<Item>> getMyItemsAsync() {
        return fetchItemListAsync("/items/user", "خطا در دریافت آگهی‌های من");
    }

    public static CompletableFuture<List<Item>> getPurchasedItemsAsync() {
        return fetchItemListAsync("/items/purchased", "خطا در دریافت لیست خریدها");
    }

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

    public static CompletableFuture<Void> approveItemAsync(Long id) {
        return runAsync(() -> {
            HttpResponse<String> res = ApiClient.put("/items/" + id + "/status?status=APPROVED", null);
            ensureSuccess(res, "خطا در تایید آگهی");
        });
    }

    public static CompletableFuture<Void> rejectItemAsync(Long id, String reason) {
        return runAsync(() -> {
            String encoded = URLEncoder.encode(reason == null ? "" : reason, StandardCharsets.UTF_8);
            HttpResponse<String> res = ApiClient.put("/items/" + id + "/status?status=REJECTED&rejectionReason=" + encoded, null);
            ensureSuccess(res, "خطا در رد آگهی");
        });
    }

    public static CompletableFuture<Void> purchaseItemAsync(Long itemId) {
        return runAsync(() -> {
            HttpResponse<String> res = ApiClient.put("/items/" + itemId + "/purchase", null);
            ensureSuccess(res, "خطا در خرید کالا");
        });
    }

    public static CompletableFuture<Void> deleteItemAsync(Long itemId) {
        return runAsync(() -> {
            HttpResponse<String> res = ApiClient.delete("/items/" + itemId);
            ensureSuccess(res, "خطا در حذف آگهی");
        });
    }

    public static CompletableFuture<Void> markAsSoldAsync(Long itemId) {
        return runAsync(() -> {
            HttpResponse<String> res = ApiClient.put("/items/" + itemId + "/sold", null);
            ensureSuccess(res, "خطا در تغییر وضعیت آگهی");
        });
    }

    // ================= Sync wrappers =================

    public static List<Item> getActiveItems() throws Exception { return joinUnwrapped(getActiveItemsAsync()); }
    public static List<Item> searchItems(String q, Long cat, Long city, Long min, Long max, String sortBy) throws Exception { return joinUnwrapped(searchItemsAsync(q, cat, city, min, max, sortBy)); }
    public static List<Item> getPendingItems() throws Exception { return joinUnwrapped(getPendingItemsAsync()); }
    public static List<Item> getUserItemsForAdmin(Long userId) throws Exception { return joinUnwrapped(getUserItemsForAdminAsync(userId)); }
    public static List<Item> getMyItems() throws Exception { return joinUnwrapped(getMyItemsAsync()); }
    public static Item getItemById(Long id) throws Exception { return joinUnwrapped(getItemByIdAsync(id)); }
    public static void approveItem(Long id) throws Exception { joinUnwrapped(approveItemAsync(id)); }
    public static void rejectItem(Long id, String reason) throws Exception { joinUnwrapped(rejectItemAsync(id, reason)); }
    public static void deleteItem(Long id) throws Exception { joinUnwrapped(deleteItemAsync(id)); }

    // ================= ثبت و ویرایش =================

    /** ثبت آگهی جدید — multipart/form-data مطابق POST /api/items/create */
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

    /** ویرایش آگهی — multipart/form-data مطابق PUT /api/items/{id} (شامل حذف/افزودن تصویر) */
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

    private interface ThrowingRunnable { void run() throws Exception; }

    private static CompletableFuture<Void> runAsync(ThrowingRunnable action) {
        return CompletableFuture.runAsync(() -> {
            try {
                action.run();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    private static <T> T joinUnwrapped(CompletableFuture<T> future) throws Exception {
        try {
            return future.join();
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

    // ================= کلاس‌های درخواست =================

    public static class ItemCreateRequest {
        public String title, description;
        public Long price, categoryId, cityId;
        public List<String> imagePaths;

        public ItemCreateRequest(String t, String d, Long p, Long cat, Long city, List<String> imgs) {
            this.title = t; this.description = d; this.price = p;
            this.categoryId = cat; this.cityId = city; this.imagePaths = imgs;
        }
    }

    public static class ItemUpdateRequest {
        public String title, description;
        public Long price, categoryId, cityId;
        public List<Long> removedImageIds;
        public List<String> newImagePaths;

        // FIX (مورد ۲): پارامتر status حذف شد. این مقدار قبلاً هرگز واقعاً به سرور ارسال
        // نمی‌شد (updateItem فقط title/description/price/categoryId/cityId را می‌فرستد) و
        // صرفاً وضعیت قدیمی و گمراه‌کننده‌ی آگهی (مثلاً "REJECTED") را بی‌مصرف حمل می‌کرد.
        public ItemUpdateRequest(String t, String d, Long p, Long cat, Long city) {
            this.title = t; this.description = d; this.price = p;
            this.categoryId = cat; this.cityId = city;
        }
    }
}