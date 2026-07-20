package com.secondhand.frontend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.model.Item;
import com.secondhand.frontend.util.ApiClient;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ItemService {
    private static final ObjectMapper objectMapper = ApiClient.getMapper();

    // === متدهای Async اصلی ===
    public static CompletableFuture<List<Item>> getActiveItemsAsync() {
        return fetchItemListAsync("/api/items/active");
    }

    public static CompletableFuture<List<Item>> searchItemsAsync(String q, Long cat, Long city, Long min, Long max) {
        String url = String.format("/api/items/search?q=%s&cat=%s&city=%s&min=%s&max=%s",
                q == null ? "" : q, cat != null ? cat : "", city != null ? city : "", min != null ? min : "", max != null ? max : "");
        return fetchItemListAsync(url);
    }

    public static CompletableFuture<List<Item>> getPendingItemsAsync() {
        return fetchItemListAsync("/api/admin/items/pending");
    }

    public static CompletableFuture<List<Item>> getUserItemsForAdminAsync(Long userId) {
        return fetchItemListAsync("/api/admin/users/" + userId + "/items");
    }

    public static CompletableFuture<List<Item>> getMyItemsAsync() {
        return ApiClient.sendRequestAsync("/api/items/my-ads", "GET")
                .thenApply(res -> {
                    try { return objectMapper.readValue(res.body(), new TypeReference<List<Item>>(){}); }
                    catch (Exception e) { throw new RuntimeException(e); }
                });
    }

    public static CompletableFuture<List<Item>> getPurchasedItemsAsync() {
        return ApiClient.sendRequestAsync("/api/items/purchased", "GET")
                .thenApply(res -> {
                    try { return objectMapper.readValue(res.body(), new TypeReference<List<Item>>(){}); }
                    catch (Exception e) { throw new RuntimeException(e); }
                });
    }

    public static CompletableFuture<Item> getItemByIdAsync(Long id) {
        return ApiClient.sendRequestAsync("/api/items/" + id, "GET")
                .thenApply(res -> {
                    try { return objectMapper.readValue(res.body(), Item.class); }
                    catch (Exception e) { throw new RuntimeException(e); }
                });
    }

    // === متدهای اصلاح شده Void (برای حذف، تایید و خرید) ===
    public static CompletableFuture<Void> approveItemAsync(Long id) {
        return ApiClient.sendRequestAsync("/api/admin/items/approve/" + id, "POST").thenAccept(res -> {});
    }

    public static CompletableFuture<Void> rejectItemAsync(Long id, String reason) {
        return ApiClient.sendRequestAsync("/api/admin/items/reject/" + id, "POST").thenAccept(res -> {});
    }

    public static CompletableFuture<Void> purchaseItemAsync(Long itemId) {
        return ApiClient.sendRequestAsync("/api/items/purchase/" + itemId, "POST").thenAccept(res -> {});
    }

    public static CompletableFuture<Void> deleteItemAsync(Long itemId) {
        return ApiClient.sendRequestAsync("/api/items/delete/" + itemId, "DELETE").thenAccept(res -> {});
    }

    public static CompletableFuture<Void> markAsSoldAsync(Long itemId) {
        return ApiClient.sendRequestAsync("/api/items/sold/" + itemId, "PUT").thenAccept(res -> {});
    }

    // === متدهای همگام (Sync Wrappers) برای سازگاری با کنترلرها ===
    public static List<Item> getActiveItems() { return getActiveItemsAsync().join(); }
    public static List<Item> searchItems(String q, Long cat, Long city, Long min, Long max) { return searchItemsAsync(q, cat, city, min, max).join(); }
    public static List<Item> getPendingItems() { return getPendingItemsAsync().join(); }
    public static void approveItem(Long id) { approveItemAsync(id).join(); }
    public static void rejectItem(Long id, String reason) { rejectItemAsync(id, reason).join(); }
    public static void deleteItem(Long id) { deleteItemAsync(id).join(); }
    public static List<Item> getUserItemsForAdmin(Long userId) { return getUserItemsForAdminAsync(userId).join(); }
    public static Item getItemById(Long id) throws Exception { return getItemByIdAsync(id).join(); }

    // === متدهای ایجاد و ویرایش ===
    public static void createItem(ItemCreateRequest request) {
        // برای حفظ سازگاری با متدهای قبلی کنترلر
        ApiClient.sendRequestAsync("/api/items", "POST").join();
    }

    public static void updateItem(Long id, ItemUpdateRequest request) {
        ApiClient.sendRequestAsync("/api/items/" + id, "PUT").join();
    }

    // === متدهای کمکی داخلی ===
    private static CompletableFuture<List<Item>> fetchItemListAsync(String endpoint) {
        return ApiClient.sendRequestAsync(endpoint, "GET")
                .thenApply(res -> {
                    try { return objectMapper.readValue(res.body(), new TypeReference<List<Item>>(){}); }
                    catch (Exception e) { throw new RuntimeException(e); }
                });
    }

    // === کلاس‌های درخواست ===
    public static class ItemCreateRequest {
        public String title, description;
        public Long price, categoryId, cityId;
        public List<String> imagePaths;
        public ItemCreateRequest(String t, String d, Long p, Long cat, Long city, List<String> imgs) {
            this.title = t; this.description = d; this.price = p; this.categoryId = cat; this.cityId = city; this.imagePaths = imgs;
        }
    }

    public static class ItemUpdateRequest {
        public String title, description, status;
        public Long price, categoryId, cityId;
        public ItemUpdateRequest(String t, String d, Long p, Long cat, Long city, String s) {
            this.title = t; this.description = d; this.price = p; this.categoryId = cat; this.cityId = city; this.status = s;
        }
    }
}