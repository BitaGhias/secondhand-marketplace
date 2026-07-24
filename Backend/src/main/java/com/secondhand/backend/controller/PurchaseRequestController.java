package com.secondhand.backend.controller;

import com.secondhand.backend.dto.purchase.PurchaseRequestResponse;
import com.secondhand.backend.security.CurrentUserService;
import com.secondhand.backend.service.PurchaseRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST controller exposing the "purchase request" API endpoints.
 * <p>
 * This class is the entry point for HTTP requests; it delegates the work to the service layer and returns the result as JSON with a proper status code. Errors are handled centrally by {@code GlobalExceptionHandler}.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
@RestController
@RequestMapping("/api/purchase-requests")
public class PurchaseRequestController {

    @Autowired private PurchaseRequestService purchaseRequestService;
    @Autowired private CurrentUserService currentUserService;

    /**
     * Creates.
     *
     * @param itemId id of the ad (item)
     * @return an HTTP response containing the operation result and a proper status code
     */
    @PostMapping("/{itemId}")
    public ResponseEntity<PurchaseRequestResponse> create(@PathVariable Long itemId) {
        PurchaseRequestResponse response = purchaseRequestService.create(itemId, currentUserService.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lists for item.
     *
     * @param itemId id of the ad (item)
     * @return an HTTP response containing the operation result and a proper status code
     */
    @GetMapping("/item/{itemId}")
    public ResponseEntity<List<PurchaseRequestResponse>> listForItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(purchaseRequestService.listForItem(itemId, currentUserService.getCurrentUserId()));
    }

    /**
     * Performs the "incoming" operation.
     *
     * @return an HTTP response containing the operation result and a proper status code
     */
    @GetMapping("/incoming")
    public ResponseEntity<List<PurchaseRequestResponse>> incoming() {
        return ResponseEntity.ok(purchaseRequestService.incoming(currentUserService.getCurrentUserId()));
    }

    /**
     * Performs the "mine" operation.
     *
     * @return an HTTP response containing the operation result and a proper status code
     */
    @GetMapping("/mine")
    public ResponseEntity<List<PurchaseRequestResponse>> mine() {
        return ResponseEntity.ok(purchaseRequestService.mine(currentUserService.getCurrentUserId()));
    }

    /**
     * Performs the "accept" operation.
     *
     * @param id unique identifier of the record
     * @return an HTTP response containing the operation result and a proper status code
     */
    @PutMapping("/{id}/accept")
    public ResponseEntity<PurchaseRequestResponse> accept(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseRequestService.accept(id, currentUserService.getCurrentUserId()));
    }

    /**
     * Performs the "decline" operation.
     *
     * @param id unique identifier of the record
     * @return an HTTP response containing the operation result and a proper status code
     */
    @PutMapping("/{id}/decline")
    public ResponseEntity<PurchaseRequestResponse> decline(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseRequestService.decline(id, currentUserService.getCurrentUserId()));
    }
}
