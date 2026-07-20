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

    public static CompletableFuture<List<Item>> getMyItemsAsync() {
        return fetchItemListAsync("/api/items/my-items");
    }

    public static CompletableFuture<List<Item>> getPurchasedItemsAsync() {
        return fetchItemListAsync("/api/items/purchased");
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
                        future.completeExceptionally(new RuntimeException("خطا در پردازش لیست کالاها"));
                    }
                }).exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    return null;
                });
        return future;
    }
}