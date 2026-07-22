package com.secondhand.backend.controller;

import com.secondhand.backend.dto.purchase.PurchaseRequestResponse;
import com.secondhand.backend.security.CurrentUserService;
import com.secondhand.backend.service.PurchaseRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/purchase-requests")
public class PurchaseRequestController {

    @Autowired private PurchaseRequestService purchaseRequestService;
    @Autowired private CurrentUserService currentUserService;

    /** ثبت درخواست خرید توسط خریدار */
    @PostMapping("/{itemId}")
    public ResponseEntity<PurchaseRequestResponse> create(@PathVariable Long itemId) {
        PurchaseRequestResponse response = purchaseRequestService.create(itemId, currentUserService.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** درخواست‌های یک آگهی (فقط صاحب آگهی) */
    @GetMapping("/item/{itemId}")
    public ResponseEntity<List<PurchaseRequestResponse>> listForItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(purchaseRequestService.listForItem(itemId, currentUserService.getCurrentUserId()));
    }

    /** همه درخواست‌های رسیده برای آگهی‌های من (برای اعلان‌ها) */
    @GetMapping("/incoming")
    public ResponseEntity<List<PurchaseRequestResponse>> incoming() {
        return ResponseEntity.ok(purchaseRequestService.incoming(currentUserService.getCurrentUserId()));
    }

    /** درخواست‌های ثبت‌شده توسط من */
    @GetMapping("/mine")
    public ResponseEntity<List<PurchaseRequestResponse>> mine() {
        return ResponseEntity.ok(purchaseRequestService.mine(currentUserService.getCurrentUserId()));
    }

    /** تایید درخواست توسط فروشنده ← فروش قطعی */
    @PutMapping("/{id}/accept")
    public ResponseEntity<PurchaseRequestResponse> accept(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseRequestService.accept(id, currentUserService.getCurrentUserId()));
    }

    /** رد درخواست توسط فروشنده */
    @PutMapping("/{id}/decline")
    public ResponseEntity<PurchaseRequestResponse> decline(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseRequestService.decline(id, currentUserService.getCurrentUserId()));
    }
}
