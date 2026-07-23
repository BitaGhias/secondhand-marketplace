package com.secondhand.frontend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.model.Category;
import com.secondhand.frontend.util.ApiClient;

import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Client-side service for "category" operations against the backend API.
 * <p>
 * This class is the client-to-server communication layer; it sends requests to the backend API through {@code ApiClient} and converts JSON responses into Java models with Jackson. On a non-successful response the server error message is propagated as an exception.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class CategoryService {

    private static final ObjectMapper objectMapper = ApiClient.getMapper();

    // دریافت همه دسته‌بندی‌ها (مسیر درست بک‌اند: /api/categories/all)
    /**
     * Gets all categories.
     *
     * @return a {@code List<Category>} with the results; empty if nothing matches
     * @throws Exception if the request fails or the server cannot be reached
     */
    public static List<Category> getAllCategories() throws Exception {
        HttpResponse<String> response = ApiClient.get("/categories/all");

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Category>>() {});
        } else {
            throw new Exception("خطا در دریافت دسته‌بندی‌ها: " + response.body());
        }
    }

    /**
     * Creates category async.
     *
     * @param name the name
     * @param parentId the "parent id" value of type {@code Long}
     * @return a {@code CompletableFuture} that completes asynchronously with the result
     */
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

    /**
     * Updates category async.
     *
     * @param id unique identifier of the record
     * @param name the name
     * @param parentId the "parent id" value of type {@code Long}
     * @return a {@code CompletableFuture} that completes asynchronously with the result
     */
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

    /**
     * Deletes category async.
     *
     * @param id unique identifier of the record
     * @return a {@code CompletableFuture} that completes asynchronously with the result
     */
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

    /**
     * Creates category.
     *
     * @param name the name
     * @param parentId the "parent id" value of type {@code Long}
     * @return the resulting {@code Category} instance
     * @throws Exception if the request fails or the server cannot be reached
     */
    public static Category createCategory(String name, Long parentId) throws Exception {
        return joinUnwrapped(createCategoryAsync(name, parentId));
    }

    /**
     * Updates category.
     *
     * @param id unique identifier of the record
     * @param name the name
     * @param parentId the "parent id" value of type {@code Long}
     * @return the resulting {@code Category} instance
     * @throws Exception if the request fails or the server cannot be reached
     */
    public static Category updateCategory(Long id, String name, Long parentId) throws Exception {
        return joinUnwrapped(updateCategoryAsync(id, name, parentId));
    }

    /**
     * Deletes category.
     *
     * @param id unique identifier of the record
     * @throws Exception if the request fails or the server cannot be reached
     */
    public static void deleteCategory(Long id) throws Exception {
        joinUnwrapped(deleteCategoryAsync(id));
    }

    /**
     * Joins unwrapped.
     *
     * @param future the future to wait for
     * @return the resulting {@code <T> T} instance
     * @throws Exception if the request fails or the server cannot be reached
     */
    private static <T> T joinUnwrapped(CompletableFuture<T> future) throws Exception {
        try {
            return future.join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof Exception ex) throw ex;
            throw e;
        }
    }

    /**
     * Ensures success.
     *
     * @param res the HTTP response
     * @param prefix prefix used in error messages
     * @throws Exception if the request fails or the server cannot be reached
     */
    private static void ensureSuccess(HttpResponse<String> res, String prefix) throws Exception {
        try {
            ApiClient.ensureSuccess(res);
        } catch (Exception ex) {
            throw new Exception(prefix + ": " + ex.getMessage(), ex);
        }
    }
}