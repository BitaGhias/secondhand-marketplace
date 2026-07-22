package com.secondhand.frontend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.model.PurchaseRequest;
import com.secondhand.frontend.util.ApiClient;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * سرویس درخواست خرید — مطابق PurchaseRequestController بک‌اند:
 *   POST /api/purchase-requests/{itemId}
 *   GET  /api/purchase-requests/item/{itemId}
 *   GET  /api/purchase-requests/incoming
 *   GET  /api/purchase-requests/mine
 *   PUT  /api/purchase-requests/{id}/accept | /decline
 */
public class PurchaseRequestService {

    private static final ObjectMapper mapper = ApiClient.getMapper();

    public static CompletableFuture<PurchaseRequest> createAsync(Long itemId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpResponse<String> res = ApiClient.post("/purchase-requests/" + itemId);
                ensureOk(res);
                return mapper.readValue(res.body(), PurchaseRequest.class);
            } catch (Exception e) { throw new CompletionException(unwrap(e)); }
        });
    }

    public static CompletableFuture<List<PurchaseRequest>> listForItemAsync(Long itemId) {
        return listAsync("/purchase-requests/item/" + itemId);
    }

    public static CompletableFuture<List<PurchaseRequest>> incomingAsync() {
        return listAsync("/purchase-requests/incoming");
    }

    public static CompletableFuture<List<PurchaseRequest>> mineAsync() {
        return listAsync("/purchase-requests/mine");
    }

    public static CompletableFuture<PurchaseRequest> acceptAsync(Long requestId) {
        return respondAsync(requestId, "accept");
    }

    public static CompletableFuture<PurchaseRequest> declineAsync(Long requestId) {
        return respondAsync(requestId, "decline");
    }

    // نسخه‌های همگام برای تردهای پس‌زمینه
    public static List<PurchaseRequest> incoming() throws Exception { return join(incomingAsync()); }
    public static List<PurchaseRequest> mine() throws Exception { return join(mineAsync()); }

    // ─── Helpers ───
    private static CompletableFuture<List<PurchaseRequest>> listAsync(String endpoint) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpResponse<String> res = ApiClient.get(endpoint);
                ensureOk(res);
                return mapper.readValue(res.body(), new TypeReference<List<PurchaseRequest>>() {});
            } catch (Exception e) { throw new CompletionException(unwrap(e)); }
        });
    }

    private static CompletableFuture<PurchaseRequest> respondAsync(Long requestId, String action) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpResponse<String> res = ApiClient.put("/purchase-requests/" + requestId + "/" + action, null);
                ensureOk(res);
                return mapper.readValue(res.body(), PurchaseRequest.class);
            } catch (Exception e) { throw new CompletionException(unwrap(e)); }
        });
    }

    private static void ensureOk(HttpResponse<String> res) throws Exception {
        ApiClient.ensureSuccess(res);
    }

    private static Exception unwrap(Exception e) {
        return e instanceof CompletionException && e.getCause() instanceof Exception c ? c : e;
    }

    private static <T> T join(CompletableFuture<T> f) throws Exception {
        try { return f.join(); }
        catch (CompletionException e) { throw e.getCause() instanceof Exception c ? c : e; }
    }

}