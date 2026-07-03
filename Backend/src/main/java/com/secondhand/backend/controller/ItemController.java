package com.secondhand.backend.controller;

import com.secondhand.backend.entity.Item;
import com.secondhand.backend.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @PostMapping("/create")
    public ResponseEntity<?> createItem(@RequestBody Item item, @RequestParam Long userId) {
        try {
            Item createdItem = itemService.createItem(item, userId);
            return ResponseEntity.ok(createdItem);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/approved")
    public ResponseEntity<List<Item>> getApprovedItems() {
        List<Item> items = itemService.getApprovedItems();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Item>> getPendingItems() {
        List<Item> items = itemService.getPendingItems();
        return ResponseEntity.ok(items);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateItemStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            Item updatedItem = itemService.updateItemStatus(id, status);
            return ResponseEntity.ok(updatedItem);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/categoey/{categoryId}")
    public ResponseEntity<List<Item>> getItemsByCategory(@PathVariable Long categoryId) {
        List<Item> items = itemService.getApprovedItemsByCategory(categoryId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getItemByUser(@PathVariable Long userId) {
        try {
            List<Item> items =  itemService.getItemByUser(userId);
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
}
