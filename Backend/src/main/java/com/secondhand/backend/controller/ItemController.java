package com.secondhand.backend.controller;

import com.secondhand.backend.dto.item.*;
import com.secondhand.backend.service.ItemService;
import com.secondhand.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired private ItemService itemService;
    @Autowired private UserService userService;

    private Long getCurrentUserId() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userService.getUserIdByUsername(userDetails.getUsername());
    }

    @PostMapping(value = "/create", consumes = "multipart/form-data")
    public ResponseEntity<ItemResponse> createItem(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("price") Long price,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam("cityId") Long cityId,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {

        Long userId = getCurrentUserId();
        ItemCreateRequest request = new ItemCreateRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setPrice(price);
        request.setCategoryId(categoryId);
        request.setCityId(cityId);
        request.setImages(images);

        return ResponseEntity.ok(itemService.addItem(request, userId));
    }

    @GetMapping("/approved")
    public ResponseEntity<List<ItemResponse>> getApprovedItems() {
        return ResponseEntity.ok(itemService.getApprovedItems());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ItemResponse>> getPendingItems() {
        return ResponseEntity.ok(itemService.getPendingItems(getCurrentUserId()));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ItemResponse> updateItemStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String rejectionReason) {
        return ResponseEntity.ok(itemService.updateItemStatus(getCurrentUserId(), id, status, rejectionReason));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ItemResponse>> getItemsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(itemService.getApprovedItemsByCategory(categoryId));
    }

    @GetMapping("/user")
    public ResponseEntity<List<ItemResponse>> getMyItems() {
        return ResponseEntity.ok(itemService.getItemByUser(getCurrentUserId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id, getCurrentUserId());
        return ResponseEntity.ok("آگهی با موفقیت حذف شد.");
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemResponse> updateItem(@PathVariable Long id, @RequestBody ItemUpdateRequest request) {
        return ResponseEntity.ok(itemService.updateItem(id, getCurrentUserId(), request));
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
        return ResponseEntity.ok(itemService.markAsSold(id, getCurrentUserId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> getItemById(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItemById(id));
    }

    @GetMapping("/{id}/images")
    public ResponseEntity<List<ImageResponse>> getItemImages(@PathVariable Long id) {
        // منطق دیتابیسی به سرویس منتقل شده است
        return ResponseEntity.ok(itemService.getItemImages(id));
    }

    @PostMapping("/search/advanced")
    public ResponseEntity<List<ItemResponse>> searchAdvanced(@RequestBody ItemSearchRequest request) {
        return ResponseEntity.ok(itemService.searchItemsAdvanced(request));
    }

    @PutMapping("/{id}/purchase")
    public ResponseEntity<ItemResponse> purchaseItem(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.purchaseItem(id, getCurrentUserId()));
    }

    @GetMapping("/purchased")
    public ResponseEntity<List<ItemResponse>> getPurchasedItems() {
        return ResponseEntity.ok(itemService.getPurchasedItems(getCurrentUserId()));
    }

    @GetMapping("/admin/user/{userId}")
    public ResponseEntity<List<ItemResponse>> getUserItemsForAdmin(@PathVariable Long userId) {
        return ResponseEntity.ok(itemService.getItemsByUserForAdmin(getCurrentUserId(), userId));
    }
}