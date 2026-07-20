package com.secondhand.frontend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.model.Item;

import java.io.File;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ItemService {

    private static final ObjectMapper objectMapper = ApiClient.getMapper();

    // دریافت لیست آگهی‌های تایید شده (approved)
    public static List<Item> getActiveItems() throws Exception {
        HttpResponse<String> response = ApiClient.get("/items/approved");

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

    // ثبت آگهی جدید (بک‌اند multipart/form-data می‌خواهد، نه JSON)
    public static Item createItem(ItemCreateRequest request) throws Exception {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("title", request.title);
        fields.put("description", request.description);
        fields.put("price", String.valueOf(request.price));
        fields.put("categoryId", String.valueOf(request.categoryId));
        fields.put("cityId", String.valueOf(request.cityId));

        List<File> files = new ArrayList<>();
        if (request.imageUrls != null) {
            for (String path : request.imageUrls) {
                if (path != null && !path.isBlank()) {
                    files.add(new File(path));
                }
            }
        }

        HttpResponse<String> response = ApiClient.postMultipart("/items/create", fields, "images", files);

        if (response.statusCode() == 200 || response.statusCode() == 201) {
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

    // اعلام فروخته شدن آگهی (مسیر درست بک‌اند: PUT /api/items/{id}/sold)
    public static Item markAsSold(Long id) throws Exception {
        HttpResponse<String> response = ApiClient.put("/items/" + id + "/sold", null);

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Item.class);
        } else {
            throw new Exception("خطا در تغییر وضعیت آگهی: " + response.body());
        }
    }

    public static List<Item> searchItems(String keyword, Long categoryId, Long cityId,
                                         Long minPrice, Long maxPrice) throws Exception {
        SearchRequest request = new SearchRequest();
        request.keyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        request.categoryId = categoryId;
        request.cityId = cityId;
        request.minPrice = minPrice;
        request.maxPrice = maxPrice;
        request.sortBy = "newest";

        HttpResponse<String> response = ApiClient.post("/items/search/advanced", request);

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Item>>() {});
        } else {
            throw new Exception("خطا در جست‌وجو: " + response.body());
        }
    }

    // دریافت آگهی‌های من (برای صفحه My Ads)
    public static List<Item> getMyItems() throws Exception {
        HttpResponse<String> response = ApiClient.get("/items/user");

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Item>>() {});
        } else {
            throw new Exception("خطا در دریافت آگهی‌های من: " + response.body());
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

    // تایید آگهی توسط ادمین (مسیر درست بک‌اند: PUT /api/items/{id}/status?status=APPROVED)
    public static void approveItem(Long itemId) throws Exception {
        HttpResponse<String> response = ApiClient.put("/items/" + itemId + "/status?status=APPROVED", null);
        if (response.statusCode() != 200) {
            throw new Exception("خطا در تایید آگهی: " + response.body());
        }
    }

    // رد آگهی توسط ادمین (مسیر درست بک‌اند: PUT /api/items/{id}/status?status=REJECTED&rejectionReason=...)
    public static void rejectItem(Long itemId, String reason) throws Exception {
        String encodedReason = URLEncoder.encode(reason == null ? "" : reason, StandardCharsets.UTF_8);
        HttpResponse<String> response = ApiClient.put(
                "/items/" + itemId + "/status?status=REJECTED&rejectionReason=" + encodedReason, null);
        if (response.statusCode() != 200) {
            throw new Exception("خطا در رد آگهی: " + response.body());
        }
    }

    // خرید آگهی (مسیر بک‌اند: PUT /api/items/{id}/purchase)
    public static Item purchaseItem(Long id) throws Exception {
        HttpResponse<String> response = ApiClient.put("/items/" + id + "/purchase", null);

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Item.class);
        } else {
            throw new Exception("خطا در خرید: " + response.body());
        }
    }

    // دریافت خریدهای کاربر جاری (GET /api/items/purchased)
    public static List<Item> getPurchasedItems() throws Exception {
        HttpResponse<String> response = ApiClient.get("/items/purchased");

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Item>>() {});
        } else {
            throw new Exception("خطا در دریافت خریدها: " + response.body());
        }
    }

    // همه آگهی‌های یک کاربر برای ادمین (GET /api/items/admin/user/{userId})
    public static List<Item> getUserItemsForAdmin(Long userId) throws Exception {
        HttpResponse<String> response = ApiClient.get("/items/admin/user/" + userId);

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Item>>() {});
        } else {
            throw new Exception("خطا در دریافت آگهی‌های کاربر: " + response.body());
        }
    }

    // ===== کلاس‌های Request =====
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

    public static class SearchRequest {
        public String keyword;
        public Long categoryId;
        public Long cityId;
        public Long minPrice;
        public Long maxPrice;
        public String sortBy;
    }
}