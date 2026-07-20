package com.secondhand.frontend.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApiClient {

    private static final String BASE_URL = "http://127.0.0.1:8080/api";
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // ⚠️ FAIL_ON_UNKNOWN_PROPERTIES غیرفعال شده تا فیلدهای اضافی پاسخ سرور باعث خطا نشوند
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static String token = null;

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

    private static String getAuthHeader() {
        return (token != null && !token.isEmpty()) ? "Bearer " + token : "";
    }

    // ===== GET =====
    public static HttpResponse<String> get(String endpoint) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Authorization", getAuthHeader())
                .header("Content-Type", "application/json")
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // ===== POST =====
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

    public static HttpResponse<String> post(String endpoint) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Authorization", getAuthHeader())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // ===== POST Multipart (برای آپلود تصویر آگهی) =====
    public static HttpResponse<String> postMultipart(String endpoint,
                                                     Map<String, String> fields,
                                                     String fileFieldName,
                                                     List<File> files) throws Exception {
        String boundary = "----SecondHandBoundary" + System.currentTimeMillis();
        String CRLF = "\r\n";
        List<byte[]> byteArrays = new ArrayList<>();

        // فیلدهای متنی
        for (Map.Entry<String, String> field : fields.entrySet()) {
            String part = "--" + boundary + CRLF +
                    "Content-Disposition: form-data; name=\"" + field.getKey() + "\"" + CRLF +
                    "Content-Type: text/plain; charset=UTF-8" + CRLF + CRLF +
                    field.getValue() + CRLF;
            byteArrays.add(part.getBytes(StandardCharsets.UTF_8));
        }

        // فایل‌ها
        if (files != null) {
            for (File file : files) {
                if (file == null || !file.exists()) continue;
                String mimeType = Files.probeContentType(file.toPath());
                if (mimeType == null) mimeType = "application/octet-stream";
                String header = "--" + boundary + CRLF +
                        "Content-Disposition: form-data; name=\"" + fileFieldName + "\"; filename=\"" + file.getName() + "\"" + CRLF +
                        "Content-Type: " + mimeType + CRLF + CRLF;
                byteArrays.add(header.getBytes(StandardCharsets.UTF_8));
                byteArrays.add(Files.readAllBytes(file.toPath()));
                byteArrays.add(CRLF.getBytes(StandardCharsets.UTF_8));
            }
        }

        byteArrays.add(("--" + boundary + "--" + CRLF).getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Authorization", getAuthHeader())
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArrays(byteArrays))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // ===== PUT =====
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

    // ===== DELETE =====
    public static HttpResponse<String> delete(String endpoint) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Authorization", getAuthHeader())
                .DELETE()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // ✅ DELETE با Body (برای delete با payload)
    public static HttpResponse<String> delete(String endpoint, Object body) throws Exception {
        String jsonBody = mapper.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Authorization", getAuthHeader())
                .header("Content-Type", "application/json")
                .method("DELETE", HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static <T> T parseResponse(String json, Class<T> clazz) throws Exception {
        return mapper.readValue(json, clazz);
    }
}
