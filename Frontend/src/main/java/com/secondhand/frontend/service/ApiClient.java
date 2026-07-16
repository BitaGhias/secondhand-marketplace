package com.secondhand.frontend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;

public class ApiClient {

    private static final String BASE_URL = "http://127.0.0.1:8080/api";
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private static String token = null;

    // ===== GETTERS =====
    public static String getBaseUrl() { return BASE_URL; }
    public static String getToken() { return token; }
    public static void setToken(String t) { token = t; }
    public static HttpClient getClient() { return client; }
    public static ObjectMapper getMapper() { return mapper; }

    public static boolean isAuthenticated() {
        return token != null && !token.isEmpty();
    }

    public static void clearToken() {
        token = null;
    }

    // ===== HEADER =====
    private static String getAuthHeader() {
        return (token != null && !token.isEmpty()) ? "Bearer " + token : "";
    }

    // ===== REQUEST METHODS =====

    // GET
    public static HttpResponse<String> get(String endpoint) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Authorization", getAuthHeader())
                .header("Content-Type", "application/json")
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // POST
    public static HttpResponse<String> post(String endpoint, Object body) throws Exception {
        String jsonBody = mapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Authorization", getAuthHeader())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // POST بدون Body
    public static HttpResponse<String> post(String endpoint) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Authorization", getAuthHeader())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // PUT
    public static HttpResponse<String> put(String endpoint, Object body) throws Exception {
        String jsonBody = body != null ? mapper.writeValueAsString(body) : "";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Authorization", getAuthHeader())
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // DELETE
    public static HttpResponse<String> delete(String endpoint) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Authorization", getAuthHeader())
                .DELETE()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // ===== HELPER: parse JSON to Object =====
    public static <T> T parseResponse(String json, Class<T> clazz) throws Exception {
        return mapper.readValue(json, clazz);
    }
}