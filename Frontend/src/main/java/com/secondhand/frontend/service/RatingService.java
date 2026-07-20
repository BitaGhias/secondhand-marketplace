package com.secondhand.frontend.service;

import com.secondhand.frontend.util.ApiClient;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class RatingService {

    public static CompletableFuture<Void> rateSellerAsync(Long itemId, int score, String comment) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            String json = String.format("{\"itemId\":%d,\"score\":%d,\"comment\":\"%s\"}", itemId, score, comment);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ApiClient.getBaseUrl() + "/api/ratings/rate"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + ApiClient.getToken())
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            ApiClient.getClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() == 200 || response.statusCode() == 201) {
                            future.complete(null);
                        } else {
                            future.completeExceptionally(new RuntimeException(response.body()));
                        }
                    });
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        return future;
    }
}