package com.secondhand.frontend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.model.Category;

import java.net.http.HttpResponse;
import java.util.List;

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
}
