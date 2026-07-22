package com.secondhand.frontend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.secondhand.frontend.model.Comment;
import com.secondhand.frontend.util.ApiClient;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CommentService {

    /**
     * دریافت نظرات یک آگهی (عمومی - بدون توکن)
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
     * ارسال نظر جدید (نیاز به توکن)
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
     * حذف نظر (فقط صاحب نظر یا ادمین)
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
     * ویرایش نظر
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