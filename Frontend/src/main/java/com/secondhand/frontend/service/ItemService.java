package com.secondhand.frontend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.model.Item;
import com.secondhand.frontend.service.ApiClient;

import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // دریافت لیست آگهی‌های فعال
    public static List<Item> getActiveItems() throws Exception {
        HttpResponse<String> response = ApiClient.get("/items");

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Item>>() {});
        } else {
            throw new Exception("خطا در دریافت آگهی‌ها: " + response.body());
        }
    }

    // دریافت جزئیات یک آگهی
    public static Item getItemById(Long id) throws Exception {
        HttpResponse<String> response = ApiClient.get("/items/" + id);

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Item.class);
        } else {
            throw new Exception("آگهی یافت نشد");
        }
    }

    // ثبت آگهی جدید
    public static Item createItem(ItemCreateRequest request) throws Exception {
        HttpResponse<String> response = ApiClient.post("/items", request);

        if (response.statusCode() == 201) {
            return objectMapper.readValue(response.body(), Item.class);
        } else {
            throw new Exception("خطا در ثبت آگهی: " + response.body());
        }
    }

    // ویرایش آگهی
    public static Item updateItem(Long id, ItemUpdateRequest request) throws Exception {
        HttpResponse<String> response = ApiClient.put("/items/" + id, request);

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Item.class);
        } else {
            throw new Exception("خطا در ویرایش آگهی: " + response.body());
        }
    }

    // حذف آگهی
    public static void deleteItem(Long id) throws Exception {
        HttpResponse<String> response = ApiClient.delete("/items/" + id);

        if (response.statusCode() != 200 && response.statusCode() != 204) {
            throw new Exception("خطا در حذف آگهی: " + response.body());
        }
    }

    // جست‌وجوی آگهی
    public static List<Item> searchItems(String keyword, Long categoryId, Long cityId,
                                         Integer minPrice, Integer maxPrice) throws Exception {
        StringBuilder url = new StringBuilder("/items/search?");
        if (keyword != null && !keyword.isEmpty()) url.append("keyword=").append(keyword).append("&");
        if (categoryId != null) url.append("categoryId=").append(categoryId).append("&");
        if (cityId != null) url.append("cityId=").append(cityId).append("&");
        if (minPrice != null) url.append("minPrice=").append(minPrice).append("&");
        if (maxPrice != null) url.append("maxPrice=").append(maxPrice).append("&");

        HttpResponse<String> response = ApiClient.get(url.toString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Item>>() {});
        } else {
            throw new Exception("خطا در جست‌وجو: " + response.body());
        }
    }

    // دریافت آگهی‌های من (برای صفحه My Ads)
    public static List<Item> getMyItems() throws Exception {
        HttpResponse<String> response = ApiClient.get("/items/my");

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Item>>() {});
        } else {
            throw new Exception("خطا در دریافت آگهی‌های من: " + response.body());
        }
    }

    // کلاس‌های Request
    public static class ItemCreateRequest {
        public String title, description;
        public Long price;
        public Long categoryId;
        public Long cityId;
        public List<String> imageUrls;

        public ItemCreateRequest(String title, String description, Long price,
                                 Long categoryId, Long cityId, List<String> imageUrls) {
            this.title = title;
            this.description = description;
            this.price = price;
            this.categoryId = categoryId;
            this.cityId = cityId;
            this.imageUrls = imageUrls;
        }
    }

    public static class ItemUpdateRequest {
        public String title, description;
        public Long price;
        public Long categoryId;
        public Long cityId;
        public String status;

        public ItemUpdateRequest(String title, String description, Long price,
                                 Long categoryId, Long cityId, String status) {
            this.title = title;
            this.description = description;
            this.price = price;
            this.categoryId = categoryId;
            this.cityId = cityId;
            this.status = status;
        }
    }
    // دریافت آگهی‌های در انتظار بررسی (برای ادمین)
    public static List<Item> getPendingItems() throws Exception {
        HttpResponse<String> response = ApiClient.get("/items/pending");
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Item>>() {});
        } else {
            throw new Exception("خطا در دریافت آگهی‌های در انتظار: " + response.body());
        }
    }

    // تایید آگهی توسط ادمین
    public static void approveItem(Long itemId) throws Exception {
        HttpResponse<String> response = ApiClient.put("/items/" + itemId + "/approve", null);
        if (response.statusCode() != 200) {
            throw new Exception("خطا در تایید آگهی: " + response.body());
        }
    }

    // رد آگهی توسط ادمین
    public static void rejectItem(Long itemId, String reason) throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("reason", reason);
        HttpResponse<String> response = ApiClient.put("/items/" + itemId + "/reject", body);
        if (response.statusCode() != 200) {
            throw new Exception("خطا در رد آگهی: " + response.body());
        }
    }
}