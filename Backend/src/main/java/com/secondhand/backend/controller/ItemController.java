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

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private CurrentUserService currentUserService;

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

    @GetMapping("/approved")
    public ResponseEntity<List<ItemResponse>> getApprovedItems() {
        return ResponseEntity.ok(itemService.getApprovedItems());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ItemResponse>> getPendingItems() {
        return ResponseEntity.ok(itemService.getPendingItems(currentUserService.getCurrentUserId()));
    }

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

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ItemResponse>> getItemsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(itemService.getApprovedItemsByCategory(categoryId));
    }

    @GetMapping("/user")
    public ResponseEntity<List<ItemResponse>> getMyItems() {
        return ResponseEntity.ok(itemService.getItemByUser(currentUserService.getCurrentUserId()));
    }

    // FIX: 200 OK -> 204 No Content
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id, currentUserService.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemResponse> updateItem(
            @PathVariable Long id,
            @RequestBody ItemUpdateRequest request
    ) {
        return ResponseEntity.ok(
                itemService.updateItem(id, currentUserService.getCurrentUserId(), request)
        );
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemResponse>> searchItems(@RequestParam String keyword) {
        return ResponseEntity.ok(itemService.searchItems(keyword));
    }

    @GetMapping("/city/{cityId}")
    public ResponseEntity<List<ItemResponse>> getItemsByCity(@PathVariable Long cityId) {
        return ResponseEntity.ok(itemService.getItemsByCity(cityId));
    }

    @PutMapping("/{id}/sold")
    public ResponseEntity<ItemResponse> markAsSold(@PathVariable Long id) {
        return ResponseEntity.ok(
                itemService.markAsSold(id, currentUserService.getCurrentUserId())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> getItemById(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItemById(id));
    }

    @GetMapping("/{id}/images")
    public ResponseEntity<List<ImageResponse>> getItemImages(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItemImages(id));
    }

    @PostMapping("/search/advanced")
    public ResponseEntity<List<ItemResponse>> searchAdvanced(@RequestBody ItemSearchRequest request) {
        return ResponseEntity.ok(itemService.searchItemsAdvanced(request));
    }

    @PutMapping("/{id}/purchase")
    public ResponseEntity<ItemResponse> purchaseItem(@PathVariable Long id) {
        return ResponseEntity.ok(
                itemService.purchaseItem(id, currentUserService.getCurrentUserId())
        );
    }

    @GetMapping("/purchased")
    public ResponseEntity<List<ItemResponse>> getPurchasedItems() {
        return ResponseEntity.ok(
                itemService.getPurchasedItems(currentUserService.getCurrentUserId())
        );
    }

    // FIX: endpoint جدید برای ادمین - دیدن هر آگهی با هر وضعیتی
    @GetMapping("/admin/{id}")
    public ResponseEntity<ItemResponse> getItemByIdForAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(
                itemService.getItemByIdForAdmin(
                        currentUserService.getCurrentUserId(), id
                )
        );
    }

    @GetMapping("/admin/user/{userId}")
    public ResponseEntity<List<ItemResponse>> getUserItemsForAdmin(@PathVariable Long userId) {
        return ResponseEntity.ok(
                itemService.getItemsByUserForAdmin(
                        currentUserService.getCurrentUserId(), userId
                )
        );
    }
}