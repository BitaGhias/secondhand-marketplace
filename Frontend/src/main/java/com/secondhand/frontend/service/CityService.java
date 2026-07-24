package com.secondhand.frontend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.model.City;
import com.secondhand.frontend.util.ApiClient;

import java.net.http.HttpResponse;
import java.util.List;

/**
 * Client-side service for "city" operations against the backend API.
 * <p>
 * This class is the client-to-server communication layer; it sends requests to the backend API through {@code ApiClient} and converts JSON responses into Java models with Jackson. On a non-successful response the server error message is propagated as an exception.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class CityService {

    // استفاده از مپر مشترک (FAIL_ON_UNKNOWN_PROPERTIES غیرفعال) تا فیلد اضافی پاسخ سرور باعث خطا نشود
    private static final ObjectMapper objectMapper = ApiClient.getMapper();

    // دریافت همه شهرها
    /**
     * Gets all cities.
     *
     * @return a {@code List<City>} with the results; empty if nothing matches
     * @throws Exception if the request fails or the server cannot be reached
     */
    public static List<City> getAllCities() throws Exception {
        HttpResponse<String> response = ApiClient.get("/cities");

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<City>>() {});
        } else {
            throw new Exception(extractErrorMessage(response));
        }
    }

    /**
     * Adds a new city; only admins are allowed and duplicate names are rejected with HTTP 400.
     *
     * @param name the name
     * @return the resulting {@code City} instance
     * @throws Exception if the request fails or the server cannot be reached
     */
    public static City addCity(String name) throws Exception {
        HttpResponse<String> response = ApiClient.post("/cities/add", java.util.Map.of("name", name));
        if (response.statusCode() == 200 || response.statusCode() == 201) {
            return objectMapper.readValue(response.body(), City.class);
        }
        throw new Exception(extractErrorMessage(response));
    }

    /**
     * Asynchronous variant of addCity that keeps the UI thread responsive.
     *
     * @param name the name
     * @return the resulting {@code java.util.concurrent.CompletableFuture<City>} instance
     */
    public static java.util.concurrent.CompletableFuture<City> addCityAsync(String name) {
        return java.util.concurrent.CompletableFuture.supplyAsync(() -> {
            try { return addCity(name); }
            catch (Exception e) { throw new RuntimeException(e.getMessage(), e); }
        });
    }

    /**
     * Extracts the error message from the "message" field of the server JSON response; falls back to a generic message with the status code.
     *
     * @param response the received response
     * @return the resulting string
     */
    private static String extractErrorMessage(HttpResponse<String> response) {
        try {
            com.fasterxml.jackson.databind.JsonNode json = objectMapper.readTree(response.body());
            if (json.has("message")) return json.get("message").asText();
        } catch (Exception ignored) {}
        return "خطای ناشناخته از سرور (کد: " + response.statusCode() + ")";
    }
}