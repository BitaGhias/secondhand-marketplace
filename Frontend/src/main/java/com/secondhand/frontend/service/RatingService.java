package com.secondhand.frontend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.model.Rating;
import com.secondhand.frontend.util.ApiClient;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Client-side service for "rating" operations against the backend API.
 * <p>
 * This class is the client-to-server communication layer; it sends requests to the backend API through {@code ApiClient} and converts JSON responses into Java models with Jackson. On a non-successful response the server error message is propagated as an exception.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class RatingService {
    private static final ObjectMapper mapper = ApiClient.getMapper();

    /**
     * Performs the "rate seller async" operation.
     *
     * @param itemId id of the ad (item)
     * @param score the rating score
     * @param comment the comment object
     * @return a {@code CompletableFuture} that completes asynchronously with the result
     */
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

    /**
     * Checks whether the "rated async" condition holds.
     *
     * @param itemId id of the ad (item)
     * @return a {@code CompletableFuture} that completes asynchronously with the result
     */
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

    /**
     * Gets seller average async.
     *
     * @param sellerId id of the seller
     * @return a {@code CompletableFuture} that completes asynchronously with the result
     */
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

    /**
     * Gets seller rating count async.
     *
     * @param sellerId id of the seller
     * @return a {@code CompletableFuture} that completes asynchronously with the result
     */
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

    /**
     * Gets seller ratings async.
     *
     * @param sellerId id of the seller
     * @return a {@code CompletableFuture} that completes asynchronously with the result
     */
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

    /**
     * Nested class used by {@code RatingService}.
     */
    public static class RatingRequest {
        public Long itemId;
        public int score;
        public String comment;
        /**
         * Performs the "rating request" operation.
         *
         * @param itemId id of the ad (item)
         * @param score the rating score
         * @param comment the comment object
         */
        public RatingRequest(Long itemId, int score, String comment) {
            this.itemId = itemId; this.score = score; this.comment = comment;
        }
    }
}