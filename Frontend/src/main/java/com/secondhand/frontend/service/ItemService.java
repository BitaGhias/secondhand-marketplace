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

    // متدهای مربوط به دریافت لیست‌ها
    public static CompletableFuture<List<Item>> getMyItemsAsync() {
        return fetchItemListAsync("/api/items/my-items");
    }

    public static CompletableFuture<List<Item>> getPurchasedItemsAsync() {
        return fetchItemListAsync("/api/items/purchased");
    }

    // متد جدید: خرید کالا
    public static CompletableFuture<Void> purchaseItemAsync(Long itemId) {
        return sendRequestAsync("/api/items/purchase/" + itemId, "POST");
    }

    // متد جدید: حذف آگهی
    public static CompletableFuture<Void> deleteItemAsync(Long itemId) {
        return sendRequestAsync("/api/items/delete/" + itemId, "DELETE");
    }

    // متد جدید: تغییر وضعیت به فروخته شده
    public static CompletableFuture<Void> markAsSoldAsync(Long itemId) {
        return sendRequestAsync("/api/items/sold/" + itemId, "PUT");
    }

    // متد کمکی برای درخواست‌های POST/PUT/DELETE
    private static CompletableFuture<Void> sendRequestAsync(String endpoint, String method) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ApiClient.getBaseUrl() + endpoint))
                .header("Authorization", "Bearer " + ApiClient.getToken())
                .method(method, HttpRequest.BodyPublishers.noBody())
                .build();

        ApiClient.getClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        future.complete(null);
                    } else {
                        future.completeExceptionally(new RuntimeException("خطای سرور: " + response.body()));
                    }
                }).exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    return null;
                });
        return future;
    }

    private static CompletableFuture<List<Item>> fetchItemListAsync(String endpoint) {
        CompletableFuture<List<Item>> future = new CompletableFuture<>();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ApiClient.getBaseUrl() + endpoint))
                .header("Authorization", "Bearer " + ApiClient.getToken())
                .GET()
                .build();

        ApiClient.getClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    try {
                        if (response.statusCode() == 200) {
                            List<Item> items = objectMapper.readValue(response.body(), new TypeReference<List<Item>>() {});
                            future.complete(items);
                        } else {
                            future.completeExceptionally(new RuntimeException(response.body()));
                        }
                    } catch (Exception e) {
                        future.completeExceptionally(e);
                    }
                }).exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    return null;
                });
        return future;
    }
}