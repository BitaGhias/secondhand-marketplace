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
import java.util.concurrent.CompletableFuture;

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

    // ===== PUT Multipart (برای ویرایش آگهی همراه با تغییر تصویر) =====
    public static HttpResponse<String> putMultipart(String endpoint,
                                                    Map<String, String> fields,
                                                    List<Long> removedImageIds,
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

        // شناسه‌های تصاویری که باید در ویرایش حذف شوند
        if (removedImageIds != null) {
            for (Long imageId : removedImageIds) {
                String part = "--" + boundary + CRLF +
                        "Content-Disposition: form-data; name=\"removedImageIds\"" + CRLF +
                        "Content-Type: text/plain; charset=UTF-8" + CRLF + CRLF +
                        imageId + CRLF;
                byteArrays.add(part.getBytes(StandardCharsets.UTF_8));
            }
        }

        // فایل‌های تصویر جدید
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
                .method("PUT", HttpRequest.BodyPublishers.ofByteArrays(byteArrays))
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

    /**
     * Extract the backend's standard Persian error message instead of exposing
     * the complete JSON response to the user.
     */
    public static String extractErrorMessage(String body) {
        if (body == null || body.isBlank()) return "خطای نامشخص از سرور دریافت شد.";
        try {
            var node = mapper.readTree(body);
            if (node.hasNonNull("message") && !node.get("message").asText().isBlank()) {
                return node.get("message").asText();
            }
        } catch (Exception ignored) {
            // Some network/proxy errors are plain text rather than JSON.
        }
        return body.trim();
    }

    /** Throw a user-readable exception for any non-2xx response. */
    public static void ensureSuccess(HttpResponse<String> response) throws Exception {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new Exception(extractErrorMessage(response.body()));
        }
    }

    public static <T> T parseResponse(String json, Class<T> clazz) throws Exception {
        return mapper.readValue(json, clazz);
    }

    public static CompletableFuture<HttpResponse<String>> sendRequestAsync(String endpoint, String method) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Authorization", getAuthHeader());

        HttpRequest request;
        if ("GET".equals(method)) {
            request = builder.GET().build();
        } else if ("POST".equals(method)) {
            request = builder.POST(HttpRequest.BodyPublishers.noBody()).build();
        } else if ("DELETE".equals(method)) {
            request = builder.DELETE().build();
        } else {
            request = builder.method(method, HttpRequest.BodyPublishers.noBody()).build();
        }

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }
}