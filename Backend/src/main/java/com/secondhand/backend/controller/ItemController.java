package com.secondhand.backend.controller;

import com.secondhand.backend.constant.ItemStatus;
import com.secondhand.backend.dto.ImageResponse;
import com.secondhand.backend.dto.ItemCreateRequest;
import com.secondhand.backend.dto.ItemResponse;
import com.secondhand.backend.dto.ItemUpdateRequest;
import com.secondhand.backend.entity.Image;
import com.secondhand.backend.entity.Item;
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


    @PostMapping(value = "/create", consumes = "multipart/form-data") // دریافت دادههای چند بخشی
    public ResponseEntity<?> createItem(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("price") Double price,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam("cityId") Long cityId,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        try {
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

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/approved")
    public ResponseEntity<List<ItemResponse>> getApprovedItems() {
        List<ItemResponse> items = itemService.getApprovedItems();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingItems() {
        try {
            Long adminId = getCurrentUserId();
            List<ItemResponse> items = itemService.getPendingItems(adminId);
            return ResponseEntity.ok(items);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateItemStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            Long adminId = getCurrentUserId();
            ItemResponse updatedItem = itemService.updateItemStatus(adminId, id, status);
            return ResponseEntity.ok(updatedItem);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ItemResponse>> getItemsByCategory(@PathVariable Long categoryId) {
        List<ItemResponse> items = itemService.getApprovedItemsByCategory(categoryId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/user")
    public ResponseEntity<?> getMyItems() {
        try {
            Long userId = getCurrentUserId();
            List<ItemResponse> items = itemService.getItemByUser(userId);
            return ResponseEntity.ok(items);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable Long id) {
        try {
            Long userId = getCurrentUserId();
            itemService.deleteItem(id, userId);
            return ResponseEntity.ok("آگهی با موفقیت حذف شد.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateItem(
            @PathVariable Long id,
            @RequestBody ItemUpdateRequest request) {
        try {
            Long userId = getCurrentUserId();
            ItemResponse updatedItem = itemService.updateItem(id, userId, request);

            return ResponseEntity.ok(updatedItem);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemResponse>> searchItems(@RequestParam String keyword) {
        List<ItemResponse> results = itemService.searchItems(keyword);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/city/{cityId}")
    public ResponseEntity<List<ItemResponse>> getItemsByCity(@PathVariable Long cityId) {
        return ResponseEntity.ok(itemService.getItemsByCity(cityId));
    }

    @PutMapping("/{id}/sold")
    public ResponseEntity<?> markAsSold(@PathVariable Long id) {
        try {
            Long userId = getCurrentUserId();
            ItemResponse updatedItem = itemService.markAsSold(id, userId);
            return ResponseEntity.ok(updatedItem);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getItemById(@PathVariable Long id) {
        try {
            ItemResponse item = itemService.getItemById(id);
            return ResponseEntity.ok(item);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/images")
    public ResponseEntity<?> getItemImages(@PathVariable Long id) {
        try {
            Item item = itemRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("آگهی یافت نشد"));

            if (item.getStatus() != ItemStatus.APPROVED) {
                throw new RuntimeException("این آگهی قابل نمایش نیست");
            }

            List<Image> images = imageRepository.findByItemId(id);
            List<ImageResponse> responses = new ArrayList<>();
            for (Image img : images) {
                responses.add(new ImageResponse(img.getId(), img.getImagePath()));
            }

            return ResponseEntity.ok(responses);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}