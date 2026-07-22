package com.secondhand.frontend.service;

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
    // ─── ثبت امتیاز ───
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

    // ─── بررسی اینکه کاربر قبلاً امتیاز داده ───
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

    // ─── میانگین امتیاز فروشنده ───
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

    public static class RatingRequest {
        public Long itemId;
        public int score;
        public String comment;
        public RatingRequest(Long itemId, int score, String comment) {
            this.itemId = itemId; this.score = score; this.comment = comment;
        }
    }
}