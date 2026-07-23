package com.secondhand.backend.controller;

import com.secondhand.backend.dto.item.ImageResponse;
import com.secondhand.backend.dto.item.ItemCreateRequest;
import com.secondhand.backend.dto.item.ItemResponse;
import com.secondhand.backend.dto.item.ItemSearchRequest;
import com.secondhand.backend.dto.item.ItemUpdateRequest;
import com.secondhand.backend.security.CurrentUserService;
import com.secondhand.backend.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST controller exposing the "item" API endpoints.
 * <p>
 * This class is the entry point for HTTP requests; it delegates the work to the service layer and returns the result as JSON with a proper status code. Errors are handled centrally by {@code GlobalExceptionHandler}.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private CurrentUserService currentUserService;

    /**
     * Creates item.
     *
     * @param title the title
     * @param description the description text
     * @param price the price (Toman)
     * @param categoryId id of the category
     * @param cityId id of the city
     * @param images list of images
     * @return an HTTP response containing the operation result and a proper status code
     */
    @PostMapping(value = "/create", consumes = "multipart/form-data")
    public ResponseEntity<ItemResponse> createItem(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("price") Long price,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam("cityId") Long cityId,
            @RequestParam(value = "images", required = false) List<MultipartFile> images
    ) {
        ItemCreateRequest request = new ItemCreateRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setPrice(price);
        request.setCategoryId(categoryId);
        request.setCityId(cityId);
        request.setImages(images);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(itemService.addItem(request, currentUserService.getCurrentUserId()));
    }

    /**
     * Gets approved items.
     *
     * @return an HTTP response containing the operation result and a proper status code
     */
    @GetMapping("/approved")
    public ResponseEntity<List<ItemResponse>> getApprovedItems() {
        return ResponseEntity.ok(itemService.getApprovedItems());
    }

    /**
     * Gets pending items.
     *
     * @return an HTTP response containing the operation result and a proper status code
     */
    @GetMapping("/pending")
    public ResponseEntity<List<ItemResponse>> getPendingItems() {
        return ResponseEntity.ok(itemService.getPendingItems(currentUserService.getCurrentUserId()));
    }

    /**
     * Updates item status.
     *
     * @param id unique identifier of the record
     * @param status the status value
     * @param rejectionReason the "rejection reason" value of type {@code String}
     * @return an HTTP response containing the operation result and a proper status code
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ItemResponse> updateItemStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String rejectionReason
    ) {
        return ResponseEntity.ok(
                itemService.updateItemStatus(
                        currentUserService.getCurrentUserId(),
                        id, status, rejectionReason
                )
        );
    }

    /**
     * Gets items by category.
     *
     * @param categoryId id of the category
     * @return an HTTP response containing the operation result and a proper status code
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ItemResponse>> getItemsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(itemService.getApprovedItemsByCategory(categoryId));
    }

    /**
     * Gets my items.
     *
     * @return an HTTP response containing the operation result and a proper status code
     */
    @GetMapping("/user")
    public ResponseEntity<List<ItemResponse>> getMyItems() {
        return ResponseEntity.ok(itemService.getItemByUser(currentUserService.getCurrentUserId()));
    }

    // FIX: 200 OK -> 204 No Content
    /**
     * Deletes item.
     *
     * @param id unique identifier of the record
     * @return an HTTP response containing the operation result and a proper status code
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id, currentUserService.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    // FIX: پشتیبانی از تغییر تصاویر هنگام ویرایش - ایندپوینت اکنون multipart/form-data می‌گیرد
    /**
     * Updates item.
     *
     * @param id unique identifier of the record
     * @param title the title
     * @param description the description text
     * @param price the price (Toman)
     * @param categoryId id of the category
     * @param cityId id of the city
     * @param removedImageIds the "removed image ids" value of type {@code List<Long>}
     * @param images list of images
     * @return an HTTP response containing the operation result and a proper status code
     */
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<ItemResponse> updateItem(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("price") Long price,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam("cityId") Long cityId,
            @RequestParam(value = "removedImageIds", required = false) List<Long> removedImageIds,
            @RequestParam(value = "images", required = false) List<MultipartFile> images
    ) {
        ItemUpdateRequest request = new ItemUpdateRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setPrice(price);
        request.setCategoryId(categoryId);
        request.setCityId(cityId);
        request.setRemovedImageIds(removedImageIds);
        request.setImages(images);

        return ResponseEntity.ok(
                itemService.updateItem(id, currentUserService.getCurrentUserId(), request)
        );
    }

    /**
     * Searches items.
     *
     * @param keyword the search keyword
     * @return an HTTP response containing the operation result and a proper status code
     */
    @GetMapping("/search")
    public ResponseEntity<List<ItemResponse>> searchItems(@RequestParam String keyword) {
        return ResponseEntity.ok(itemService.searchItems(keyword));
    }

    /**
     * Gets items by city.
     *
     * @param cityId id of the city
     * @return an HTTP response containing the operation result and a proper status code
     */
    @GetMapping("/city/{cityId}")
    public ResponseEntity<List<ItemResponse>> getItemsByCity(@PathVariable Long cityId) {
        return ResponseEntity.ok(itemService.getItemsByCity(cityId));
    }

    /**
     * Marks as sold.
     *
     * @param id unique identifier of the record
     * @return an HTTP response containing the operation result and a proper status code
     */
    @PutMapping("/{id}/sold")
    public ResponseEntity<ItemResponse> markAsSold(@PathVariable Long id) {
        return ResponseEntity.ok(
                itemService.markAsSold(id, currentUserService.getCurrentUserId())
        );
    }

    /**
     * Gets item by id.
     *
     * @param id unique identifier of the record
     * @return an HTTP response containing the operation result and a proper status code
     */
    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> getItemById(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItemById(id));
    }

    /**
     * Gets item images.
     *
     * @param id unique identifier of the record
     * @return an HTTP response containing the operation result and a proper status code
     */
    @GetMapping("/{id}/images")
    public ResponseEntity<List<ImageResponse>> getItemImages(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItemImages(id));
    }

    /**
     * Searches advanced.
     *
     * @param request request body received from the client
     * @return an HTTP response containing the operation result and a proper status code
     */
    @PostMapping("/search/advanced")
    public ResponseEntity<List<ItemResponse>> searchAdvanced(@RequestBody ItemSearchRequest request) {
        return ResponseEntity.ok(itemService.searchItemsAdvanced(request));
    }

    /**
     * Direct purchase of an ad; the ad status changes to SOLD and all remaining open purchase requests are rejected.
     *
     * @param id unique identifier of the record
     * @return an HTTP response containing the operation result and a proper status code
     */
    @PutMapping("/{id}/purchase")
    public ResponseEntity<ItemResponse> purchaseItem(@PathVariable Long id) {
        return ResponseEntity.ok(
                itemService.purchaseItem(id, currentUserService.getCurrentUserId())
        );
    }

    /**
     * Gets purchased items.
     *
     * @return an HTTP response containing the operation result and a proper status code
     */
    @GetMapping("/purchased")
    public ResponseEntity<List<ItemResponse>> getPurchasedItems() {
        return ResponseEntity.ok(
                itemService.getPurchasedItems(currentUserService.getCurrentUserId())
        );
    }

    // FIX: endpoint جدید برای ادمین - دیدن هر آگهی با هر وضعیتی
    /**
     * Gets item by id for admin.
     *
     * @param id unique identifier of the record
     * @return an HTTP response containing the operation result and a proper status code
     */
    @GetMapping("/admin/{id}")
    public ResponseEntity<ItemResponse> getItemByIdForAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(
                itemService.getItemByIdForAdmin(
                        currentUserService.getCurrentUserId(), id
                )
        );
    }

    /**
     * Gets user items for admin.
     *
     * @param userId id of the user
     * @return an HTTP response containing the operation result and a proper status code
     */
    @GetMapping("/admin/user/{userId}")
    public ResponseEntity<List<ItemResponse>> getUserItemsForAdmin(@PathVariable Long userId) {
        return ResponseEntity.ok(
                itemService.getItemsByUserForAdmin(
                        currentUserService.getCurrentUserId(), userId
                )
        );
    }
}