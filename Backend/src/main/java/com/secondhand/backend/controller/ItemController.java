package com.secondhand.backend.controller;

import com.secondhand.backend.constant.ItemStatus;
import com.secondhand.backend.dto.item.*;
import com.secondhand.backend.entity.Image;
import com.secondhand.backend.entity.Item;
import com.secondhand.backend.exception.custom.BadRequestException;
import com.secondhand.backend.exception.custom.ResourceNotFoundException;
import com.secondhand.backend.repository.ImageRepository;
import com.secondhand.backend.repository.ItemRepository;
import com.secondhand.backend.service.ItemService;
import com.secondhand.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ItemRepository itemRepository;

    private Long getCurrentUserId() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        return userService.getUserIdByUsername(username);
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

        ItemResponse createdItem = itemService.addItem(request, userId);
        return ResponseEntity.ok(createdItem);
    }

    @GetMapping("/approved")
    public ResponseEntity<List<ItemResponse>> getApprovedItems() {
        List<ItemResponse> items = itemService.getApprovedItems();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ItemResponse>> getPendingItems() {
        Long adminId = getCurrentUserId();
        List<ItemResponse> items = itemService.getPendingItems(adminId);
        return ResponseEntity.ok(items);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ItemResponse> updateItemStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String rejectionReason) {
        Long adminId = getCurrentUserId();
        ItemResponse updatedItem = itemService.updateItemStatus(adminId, id, status, rejectionReason);
        return ResponseEntity.ok(updatedItem);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ItemResponse>> getItemsByCategory(@PathVariable Long categoryId) {
        List<ItemResponse> items = itemService.getApprovedItemsByCategory(categoryId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/user")
    public ResponseEntity<List<ItemResponse>> getMyItems() {
        Long userId = getCurrentUserId();
        List<ItemResponse> items = itemService.getItemByUser(userId);
        return ResponseEntity.ok(items);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteItem(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        itemService.deleteItem(id, userId);
        return ResponseEntity.ok("آگهی با موفقیت حذف شد.");
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemResponse> updateItem(
            @PathVariable Long id,
            @RequestBody ItemUpdateRequest request) {
        Long userId = getCurrentUserId();
        ItemResponse updatedItem = itemService.updateItem(id, userId, request);
        return ResponseEntity.ok(updatedItem);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemResponse>> searchItems(@RequestParam String keyword) {
        List<ItemResponse> results = itemService.searchItems(keyword);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/city/{cityId}")
    public ResponseEntity<List<ItemResponse>> getItemsByCity(@PathVariable Long cityId) {
        List<ItemResponse> items = itemService.getItemsByCity(cityId);
        return ResponseEntity.ok(items);
    }

    @PutMapping("/{id}/sold")
    public ResponseEntity<ItemResponse> markAsSold(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        ItemResponse updatedItem = itemService.markAsSold(id, userId);
        return ResponseEntity.ok(updatedItem);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> getItemById(@PathVariable Long id) {
        ItemResponse item = itemService.getItemById(id);
        return ResponseEntity.ok(item);
    }

    @GetMapping("/{id}/images")
    public ResponseEntity<List<ImageResponse>> getItemImages(@PathVariable Long id) {

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("آگهی یافت نشد"));

        if (item.getStatus() != ItemStatus.APPROVED && item.getStatus() != ItemStatus.SOLD) {
            throw new BadRequestException("این آگهی قابل نمایش نیست");
        }

        List<Image> images = imageRepository.findByItemId(id);
        List<ImageResponse> responses = new ArrayList<>();
        for (Image img : images) {
            responses.add(new ImageResponse(img.getId(), img.getImagePath()));
        }

        return ResponseEntity.ok(responses);
    }

    @PostMapping("/search/advanced")
    public ResponseEntity<List<ItemResponse>> searchAdvanced(@RequestBody ItemSearchRequest request) {
        List<ItemResponse> results = itemService.searchItemsAdvanced(request);
        return ResponseEntity.ok(results);
    }

    // خرید آگهی توسط خریدار
    @PutMapping("/{id}/purchase")
    public ResponseEntity<ItemResponse> purchaseItem(@PathVariable Long id) {
        Long buyerId = getCurrentUserId();
        ItemResponse purchased = itemService.purchaseItem(id, buyerId);
        return ResponseEntity.ok(purchased);
    }

    // لیست خریدهای کاربر جاری
    @GetMapping("/purchased")
    public ResponseEntity<List<ItemResponse>> getPurchasedItems() {
        Long userId = getCurrentUserId();
        List<ItemResponse> items = itemService.getPurchasedItems(userId);
        return ResponseEntity.ok(items);
    }

    // همه آگهی‌های یک کاربر (فقط ادمین)
    @GetMapping("/admin/user/{userId}")
    public ResponseEntity<List<ItemResponse>> getUserItemsForAdmin(@PathVariable Long userId) {
        Long adminId = getCurrentUserId();
        List<ItemResponse> items = itemService.getItemsByUserForAdmin(adminId, userId);
        return ResponseEntity.ok(items);
    }
}
