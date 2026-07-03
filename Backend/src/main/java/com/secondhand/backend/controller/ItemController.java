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
}
