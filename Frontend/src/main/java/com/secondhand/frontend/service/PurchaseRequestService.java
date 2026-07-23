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
 * Client-side service for "purchase request" operations against the backend API.
 * <p>
 * This class is the client-to-server communication layer; it sends requests to the backend API through {@code ApiClient} and converts JSON responses into Java models with Jackson. On a non-successful response the server error message is propagated as an exception.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class PurchaseRequestService {

    private static final ObjectMapper mapper = ApiClient.getMapper();

    /**
     * Creates async.
     *
     * @param itemId id of the ad (item)
     * @return a {@code CompletableFuture} that completes asynchronously with the result
     */
    public static CompletableFuture<PurchaseRequest> createAsync(Long itemId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpResponse<String> res = ApiClient.post("/purchase-requests/" + itemId);
                ensureOk(res);
                return mapper.readValue(res.body(), PurchaseRequest.class);
            } catch (Exception e) { throw new CompletionException(unwrap(e)); }
        });
    }

    /**
     * Lists for item async.
     *
     * @param itemId id of the ad (item)
     * @return a {@code CompletableFuture} that completes asynchronously with the result
     */
    public static CompletableFuture<List<PurchaseRequest>> listForItemAsync(Long itemId) {
        return listAsync("/purchase-requests/item/" + itemId);
    }

    /**
     * Performs the "incoming async" operation.
     *
     * @return a {@code CompletableFuture} that completes asynchronously with the result
     */
    public static CompletableFuture<List<PurchaseRequest>> incomingAsync() {
        return listAsync("/purchase-requests/incoming");
    }

    /**
     * Performs the "mine async" operation.
     *
     * @return a {@code CompletableFuture} that completes asynchronously with the result
     */
    public static CompletableFuture<List<PurchaseRequest>> mineAsync() {
        return listAsync("/purchase-requests/mine");
    }

    /**
     * Performs the "accept async" operation.
     *
     * @param requestId id of the request
     * @return a {@code CompletableFuture} that completes asynchronously with the result
     */
    public static CompletableFuture<PurchaseRequest> acceptAsync(Long requestId) {
        return respondAsync(requestId, "accept");
    }

    /**
     * Performs the "decline async" operation.
     *
     * @param requestId id of the request
     * @return a {@code CompletableFuture} that completes asynchronously with the result
     */
    public static CompletableFuture<PurchaseRequest> declineAsync(Long requestId) {
        return respondAsync(requestId, "decline");
    }

    // نسخه‌های همگام برای تردهای پس‌زمینه
    /**
     * Performs the "incoming" operation.
     *
     * @return a {@code List<PurchaseRequest>} with the results; empty if nothing matches
     */
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

    /**
     * Performs the "respond async" operation.
     *
     * @param requestId id of the request
     * @param action the operation to execute
     * @return a {@code CompletableFuture} that completes asynchronously with the result
     */
    private static CompletableFuture<PurchaseRequest> respondAsync(Long requestId, String action) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpResponse<String> res = ApiClient.put("/purchase-requests/" + requestId + "/" + action, null);
                ensureOk(res);
                return mapper.readValue(res.body(), PurchaseRequest.class);
            } catch (Exception e) { throw new CompletionException(unwrap(e)); }
        });
    }

    /**
     * Ensures ok.
     *
     * @param res the HTTP response
     * @throws Exception if the request fails or the server cannot be reached
     */
    private static void ensureOk(HttpResponse<String> res) throws Exception {
        ApiClient.ensureSuccess(res);
    }

    /**
     * Performs the "unwrap" operation.
     *
     * @param e the exception/event that occurred
     * @return the resulting {@code Exception} instance
     */
    private static Exception unwrap(Exception e) {
        return e instanceof CompletionException && e.getCause() instanceof Exception c ? c : e;
    }

    /**
     * Joins.
     *
     * @param f the "f" value of type {@code CompletableFuture<T>}
     * @return the resulting {@code <T> T} instance
     * @throws Exception if the request fails or the server cannot be reached
     */
    private static <T> T join(CompletableFuture<T> f) throws Exception {
        try { return f.join(); }
        catch (CompletionException e) { throw e.getCause() instanceof Exception c ? c : e; }
    }

}