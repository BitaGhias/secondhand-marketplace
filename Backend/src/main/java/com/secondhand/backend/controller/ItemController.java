package com.secondhand.backend.controller;

import com.secondhand.backend.dto.ItemCreateRequest;
import com.secondhand.backend.dto.ItemResponse;
import com.secondhand.backend.service.ItemService;
import com.secondhand.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    private Long getCurrentUserId() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        return userService.getUserIdByUsername(username);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createItem(@RequestBody ItemCreateRequest request) {
        try {
            Long userId = getCurrentUserId();
            ItemResponse createdItem = itemService.addItem(request , userId);
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
    public ResponseEntity<?> getPendingItems(@RequestParam Long adminId) {
        try {
            List<ItemResponse> items = itemService.getPendingItems(adminId);
            return ResponseEntity.ok(items);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateItemStatus(
            @PathVariable Long id,
            @RequestParam Long adminId,
            @RequestParam String status) {
        try {
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

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getItemByUser(@PathVariable Long userId) {
        try {
            List<ItemResponse> items = itemService.getItemByUser(userId);
            return ResponseEntity.ok(items);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable Long id, @RequestParam Long userId) {
        try {
            itemService.deleteItem(id, userId);
            return ResponseEntity.ok("آگهی با موفقیت حذف شد.");
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
    public ResponseEntity<?> markAsSold(@PathVariable Long id, @RequestParam Long userId) {
        try {
            ItemResponse updatedItem = itemService.markAsSold(id, userId);
            return ResponseEntity.ok(updatedItem);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}