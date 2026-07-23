package com.secondhand.frontend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.model.Rating;
import com.secondhand.frontend.util.ApiClient;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class RatingService {
    private static final ObjectMapper mapper = ApiClient.getMapper();

    public static CompletableFuture<Void> rateSellerAsync(Long itemId, int score, String comment) {
        return CompletableFuture.runAsync(() -> {
            try {
                RatingRequest req = new RatingRequest(itemId, score, comment == null ? "" : comment);
                HttpResponse<String> res = ApiClient.post("/ratings/add", req);
                ApiClient.ensureSuccess(res);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    public static CompletableFuture<Boolean> hasRatedAsync(Long itemId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpResponse<String> res = ApiClient.get("/ratings/item/" + itemId + "/rated");
                ApiClient.ensureSuccess(res);
                return Boolean.parseBoolean(res.body().trim());
            } catch (Exception e) {
                return false;
            }
        });
    }

    public static CompletableFuture<Double> getSellerAverageAsync(Long sellerId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpResponse<String> res = ApiClient.get("/ratings/seller/" + sellerId + "/average");
                ApiClient.ensureSuccess(res);
                return Double.parseDouble(res.body().trim());
            } catch (Exception e) {
                return 0.0;
            }
        });
    }

    public static CompletableFuture<Long> getSellerRatingCountAsync(Long sellerId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpResponse<String> res = ApiClient.get("/ratings/seller/" + sellerId + "/count");
                ApiClient.ensureSuccess(res);
                return Long.parseLong(res.body().trim());
            } catch (Exception e) {
                return 0L;
            }
        });
    }

    public static CompletableFuture<List<Rating>> getSellerRatingsAsync(Long sellerId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpResponse<String> res = ApiClient.get("/ratings/seller/" + sellerId);
                ApiClient.ensureSuccess(res);
                return mapper.readValue(res.body(), new TypeReference<List<Rating>>() {});
            } catch (Exception e) {
                return List.of();
            }
        });
    }

    public static class RatingRequest {
        public Long itemId;
        public int score;
        public String comment;
        public RatingRequest(Long itemId, int score, String comment) {
            this.itemId = itemId; this.score = score; this.comment = comment;
        }
    }
}