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
        if (response.statusCode() != 200) {
            throw new RuntimeException("خطا در دریافت نظرات: کد " + response.statusCode());
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
                if (response.statusCode() != 201) {
                    String msg = extractErrorMessage(response.body());
                    throw new RuntimeException(msg);
                }
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
                if (response.statusCode() != 204 && response.statusCode() != 200) {
                    String msg = extractErrorMessage(response.body());
                    throw new RuntimeException(msg);
                }
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
                if (response.statusCode() != 200) {
                    String msg = extractErrorMessage(response.body());
                    throw new RuntimeException(msg);
                }
                return ApiClient.getMapper().readValue(response.body(), Comment.class);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });
    }

    private static String extractErrorMessage(String responseBody) {
        try {
            Map<?, ?> map = ApiClient.getMapper().readValue(responseBody, Map.class);
            Object msg = map.get("message");
            if (msg != null) return msg.toString();
        } catch (Exception ignored) {}
        return responseBody;
    }
}
