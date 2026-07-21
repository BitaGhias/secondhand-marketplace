package com.secondhand.frontend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.util.ApiClient;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * سرویس امتیازدهی — مطابق RatingController بک‌اند:
 *   POST /api/ratings/add   با بدنه {itemId, score, comment}
 */
public class RatingService {
    private static final ObjectMapper objectMapper = ApiClient.getMapper();

    public static CompletableFuture<Void> rateSellerAsync(Long itemId, int score, String comment) {
        return CompletableFuture.runAsync(() -> {
            try {
                RatingRequest request = new RatingRequest(itemId, score, comment == null ? "" : comment);
                HttpResponse<String> res = ApiClient.post("/ratings/add", request);
                if (res.statusCode() < 200 || res.statusCode() >= 300) {
                    throw new Exception(extractMessage(res.body()));
                }
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    private static String extractMessage(String body) {
        try {
            JsonNode node = objectMapper.readTree(body);
            if (node.has("message")) return node.get("message").asText();
        } catch (Exception ignored) {
        }
        return body != null && !body.isBlank() ? body : "خطا در ثبت امتیاز";
    }

    public static class RatingRequest {
        public Long itemId;
        public int score;
        public String comment;

        public RatingRequest(Long itemId, int score, String comment) {
            this.itemId = itemId;
            this.score = score;
            this.comment = comment;
        }
    }
}
