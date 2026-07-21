package com.secondhand.frontend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.util.ApiClient;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * سرویس امتیازدهی — مطابق RatingController بک‌اند:
 *   POST /api/ratings/add
 *   GET  /api/ratings/item/{itemId}/rated    (مرحله ۳)
 *   GET  /api/ratings/seller/{sellerId}/average
 */
public class RatingService {
    private static final ObjectMapper mapper = ApiClient.getMapper();

    // ─── ثبت امتیاز ───
    public static CompletableFuture<Void> rateSellerAsync(Long itemId, int score, String comment) {
        return CompletableFuture.runAsync(() -> {
            try {
                RatingRequest req = new RatingRequest(itemId, score, comment == null ? "" : comment);
                HttpResponse<String> res = ApiClient.post("/ratings/add", req);
                if (res.statusCode() < 200 || res.statusCode() >= 300)
                    throw new Exception(extractMessage(res.body()));
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    // ─── بررسی اینکه کاربر قبلاً امتیاز داده ───
    public static CompletableFuture<Boolean> hasRatedAsync(Long itemId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpResponse<String> res = ApiClient.get("/ratings/item/" + itemId + "/rated");
                if (res.statusCode() == 200) {
                    return Boolean.parseBoolean(res.body().trim());
                }
                return false;
            } catch (Exception e) {
                return false;
            }
        });
    }

    // ─── میانگین امتیاز فروشنده ───
    public static CompletableFuture<Double> getSellerAverageAsync(Long sellerId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpResponse<String> res = ApiClient.get("/ratings/seller/" + sellerId + "/average");
                if (res.statusCode() == 200) {
                    return Double.parseDouble(res.body().trim());
                }
                return 0.0;
            } catch (Exception e) {
                return 0.0;
            }
        });
    }

    // ─── Helpers ───
    private static String extractMessage(String body) {
        try {
            JsonNode node = mapper.readTree(body);
            if (node.has("message")) return node.get("message").asText();
        } catch (Exception ignored) {}
        return body != null && !body.isBlank() ? body : "خطا در ثبت امتیاز";
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
