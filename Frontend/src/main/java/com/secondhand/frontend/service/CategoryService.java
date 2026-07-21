package com.secondhand.frontend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.model.Category;
import com.secondhand.frontend.util.ApiClient;

import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class CategoryService {

    private static final ObjectMapper objectMapper = ApiClient.getMapper();

    // دریافت همه دسته‌بندی‌ها (مسیر درست بک‌اند: /api/categories/all)
    public static List<Category> getAllCategories() throws Exception {
        HttpResponse<String> response = ApiClient.get("/categories/all");

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Category>>() {});
        } else {
            throw new Exception("خطا در دریافت دسته‌بندی‌ها: " + response.body());
        }
    }

    /** ایجاد دسته‌بندی جدید — POST /api/categories/create */
    public static CompletableFuture<Category> createCategoryAsync(String name, Long parentId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("name", name);
                body.put("parentId", parentId);
                HttpResponse<String> res = ApiClient.post("/categories/create", body);
                ensureSuccess(res, "خطا در ایجاد دسته‌بندی");
                return objectMapper.readValue(res.body(), Category.class);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    /** ویرایش دسته‌بندی — PUT /api/categories/{id} */
    public static CompletableFuture<Category> updateCategoryAsync(Long id, String name, Long parentId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("name", name);
                body.put("parentId", parentId);
                HttpResponse<String> res = ApiClient.put("/categories/" + id, body);
                ensureSuccess(res, "خطا در ویرایش دسته‌بندی");
                return objectMapper.readValue(res.body(), Category.class);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    /** حذف دسته‌بندی — DELETE /api/categories/{id} */
    public static CompletableFuture<Void> deleteCategoryAsync(Long id) {
        return CompletableFuture.runAsync(() -> {
            try {
                HttpResponse<String> res = ApiClient.delete("/categories/" + id);
                ensureSuccess(res, "خطا در حذف دسته‌بندی");
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    // ================= Sync wrappers =================

    public static Category createCategory(String name, Long parentId) throws Exception {
        return joinUnwrapped(createCategoryAsync(name, parentId));
    }

    public static Category updateCategory(Long id, String name, Long parentId) throws Exception {
        return joinUnwrapped(updateCategoryAsync(id, name, parentId));
    }

    public static void deleteCategory(Long id) throws Exception {
        joinUnwrapped(deleteCategoryAsync(id));
    }

    private static <T> T joinUnwrapped(CompletableFuture<T> future) throws Exception {
        try {
            return future.join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof Exception ex) throw ex;
            throw e;
        }
    }

    private static void ensureSuccess(HttpResponse<String> res, String prefix) throws Exception {
        if (res.statusCode() < 200 || res.statusCode() >= 300) {
            throw new Exception(prefix + ": " + extractMessage(res.body()));
        }
    }

    private static String extractMessage(String body) {
        try {
            JsonNode node = objectMapper.readTree(body);
            if (node.has("message")) return node.get("message").asText();
        } catch (Exception ignored) {
        }
        return body != null && !body.isBlank() ? body : "خطای ناشناخته";
    }
}