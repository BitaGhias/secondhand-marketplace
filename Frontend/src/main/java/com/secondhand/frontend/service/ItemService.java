package com.secondhand.frontend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.model.Item;
import com.secondhand.frontend.util.ApiClient;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ItemService {
    private static final ObjectMapper objectMapper = ApiClient.getMapper();

    // متدهای عمومی
    public static CompletableFuture<List<Item>> getActiveItemsAsync() {
        return fetchItemListAsync("/api/items/active");
    }

    public static CompletableFuture<List<Item>> searchItemsAsync(String q, Long cat, Long city, Long min, Long max) {
        String url = String.format("/api/items/search?q=%s&cat=%s&city=%s&min=%s&max=%s",
                q == null ? "" : q, cat != null ? cat : "", city != null ? city : "", min != null ? min : "", max != null ? max : "");
        return fetchItemListAsync(url);
    }

    // متدهای ادمین
    public static CompletableFuture<List<Item>> getPendingItemsAsync() {
        return fetchItemListAsync("/api/admin/items/pending");
    }

    public static CompletableFuture<Void> approveItemAsync(Long id) {
        return sendRequestAsync("/api/admin/items/approve/" + id, "POST");
    }

    public static CompletableFuture<Void> rejectItemAsync(Long id, String reason) {
        // فرض بر این است که API شما برای رد کردن، دلیل را در بدنه می‌پذیرد
        return sendRequestAsync("/api/admin/items/reject/" + id, "POST");
    }

    public static CompletableFuture<List<Item>> getUserItemsForAdminAsync(Long userId) {
        return fetchItemListAsync("/api/admin/users/" + userId + "/items");
    }

    // متدهای قبلی (خرید و حذف)
    public static CompletableFuture<Void> purchaseItemAsync(Long itemId) { return sendRequestAsync("/api/items/purchase/" + itemId, "POST"); }
    public static CompletableFuture<Void> deleteItemAsync(Long itemId) { return sendRequestAsync("/api/items/delete/" + itemId, "DELETE"); }
    public static CompletableFuture<Void> markAsSoldAsync(Long itemId) { return sendRequestAsync("/api/items/sold/" + itemId, "PUT"); }

    // متدهای کمکی (همانطور که در پیام قبلی داشتید)
    private static CompletableFuture<Void> sendRequestAsync(String endpoint, String method) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ApiClient.getBaseUrl() + endpoint))
                .header("Authorization", "Bearer " + ApiClient.getToken())
                .method(method, HttpRequest.BodyPublishers.noBody())
                .build();
        ApiClient.getClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(res -> { if (res.statusCode() >= 200 && res.statusCode() < 300) future.complete(null);
                else future.completeExceptionally(new RuntimeException(res.body())); })
                .exceptionally(ex -> { future.completeExceptionally(ex); return null; });
        return future;
    }

    private static CompletableFuture<List<Item>> fetchItemListAsync(String endpoint) {
        CompletableFuture<List<Item>> future = new CompletableFuture<>();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ApiClient.getBaseUrl() + endpoint))
                .header("Authorization", "Bearer " + ApiClient.getToken())
                .GET().build();
        ApiClient.getClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(res -> {
                    try { if (res.statusCode() == 200) future.complete(objectMapper.readValue(res.body(), new TypeReference<>(){}));
                    else future.completeExceptionally(new RuntimeException(res.body())); }
                    catch (Exception e) { future.completeExceptionally(e); }
                }).exceptionally(ex -> { future.completeExceptionally(ex); return null; });
        return future;
    }

    public static List<Item> getActiveItems() {
        return getActiveItemsAsync().join(); // تبدیل Async به همگام برای رفع خطا
    }

    public static List<Item> searchItems(String q, Long cat, Long city, Long min, Long max) {
        return searchItemsAsync(q, cat, city, min, max).join(); // تبدیل Async به همگام
    }

    // === متدهای همگام واسط (Synchronous Wrappers) برای رفع خطای ادمین ===

    public static java.util.List<Item> getPendingItems() {
        return getPendingItemsAsync().join();
    }

    public static void approveItem(Long id) {
        approveItemAsync(id).join();
    }

    public static void rejectItem(Long id, String reason) {
        rejectItemAsync(id, reason).join();
    }

    public static void deleteItem(Long id) {
        deleteItemAsync(id).join();
    }

    public static java.util.List<Item> getUserItemsForAdmin(Long userId) {
        return getUserItemsForAdminAsync(userId).join();
    }

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

    // متدهای سرویس برای CreateAdController
    public static void createItem(ItemCreateRequest request) {
        // برای سادگی فعلاً با join انجام می‌دهیم تا کد شما بدون تغییر ساختار کار کند
        sendRequestAsync("/api/items", "POST").join();
    }

    public static void updateItem(Long id, ItemUpdateRequest request) {
        sendRequestAsync("/api/items/" + id, "PUT").join();
    }
}