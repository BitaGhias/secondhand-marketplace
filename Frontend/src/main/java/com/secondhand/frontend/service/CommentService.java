package com.secondhand.frontend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.secondhand.frontend.model.Comment;
import com.secondhand.frontend.util.ApiClient;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Client-side service for "comment" operations against the backend API.
 * <p>
 * This class is the client-to-server communication layer; it sends requests to the backend API through {@code ApiClient} and converts JSON responses into Java models with Jackson. On a non-successful response the server error message is propagated as an exception.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class CommentService {

    /**
     * Gets comments.
     *
     * @param itemId id of the ad (item)
     * @return a {@code List<Comment>} with the results; empty if nothing matches
     * @throws Exception if the request fails or the server cannot be reached
     */
    public static List<Comment> getComments(Long itemId) throws Exception {
        HttpResponse<String> response = ApiClient.get("/comments/item/" + itemId);
        try {
            ApiClient.ensureSuccess(response);
        } catch (Exception ex) {
            throw new RuntimeException("خطا در دریافت نظرات: " + ex.getMessage(), ex);
        }
        return ApiClient.getMapper().readValue(
                response.body(),
                new TypeReference<List<Comment>>() {}
        );
    }

    /**
     * Adds comment.
     *
     * @param itemId id of the ad (item)
     * @param text the text value
     * @return a {@code CompletableFuture} that completes asynchronously with the result
     */
    public static CompletableFuture<Comment> addComment(Long itemId, String text) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> body = Map.of("itemId", itemId, "text", text);
                HttpResponse<String> response = ApiClient.post("/comments/add", body);
                ApiClient.ensureSuccess(response);
                return ApiClient.getMapper().readValue(response.body(), Comment.class);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });
    }

    /**
     * Deletes comment.
     *
     * @param commentId id of the comment
     * @return a {@code CompletableFuture} that completes asynchronously with the result
     */
    public static CompletableFuture<Void> deleteComment(Long commentId) {
        return CompletableFuture.runAsync(() -> {
            try {
                HttpResponse<String> response = ApiClient.delete("/comments/" + commentId);
                ApiClient.ensureSuccess(response);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });
    }

    /**
     * Edits comment.
     *
     * @param commentId id of the comment
     * @param newText the "new text" value of type {@code String}
     * @return a {@code CompletableFuture} that completes asynchronously with the result
     */
    public static CompletableFuture<Comment> editComment(Long commentId, String newText) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpResponse<String> response = ApiClient.put(
                        "/comments/" + commentId + "?text=" + java.net.URLEncoder.encode(newText, "UTF-8"),
                        null
                );
                ApiClient.ensureSuccess(response);
                return ApiClient.getMapper().readValue(response.body(), Comment.class);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });
    }

}