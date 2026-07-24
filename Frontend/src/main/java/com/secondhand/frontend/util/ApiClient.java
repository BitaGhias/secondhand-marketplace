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

/**
 * Low-level HTTP client used by all frontend services; builds requests, attaches the JWT token and parses responses with a shared Jackson mapper.
 * <p>
 * This class is a helper utility whose methods are used across different parts of the application.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class ApiClient {

    private static final String DEFAULT_BASE_URL = "http://127.0.0.1:8080/api";
    private static final String BASE_URL = resolveBaseUrl();
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // ⚠️ FAIL_ON_UNKNOWN_PROPERTIES غیرفعال شده تا فیلدهای اضافی پاسخ سرور باعث خطا نشوند
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static String token = null;

    /**
     * Performs the "resolve base url" operation.
     *
     * @return the resulting string
     */
    private static String resolveBaseUrl() {
        String configured = System.getProperty("secondhand.api.url");
        if (configured == null || configured.isBlank()) {
            configured = System.getenv("SECONDHAND_API_URL");
        }
        if (configured == null || configured.isBlank()) {
            return DEFAULT_BASE_URL;
        }

        String normalized = configured.trim().replaceAll("/+$", "");
        return normalized.endsWith("/api") ? normalized : normalized + "/api";
    }

    /**
     * Gets base url.
     *
     * @return the resulting string
     */
    public static String getBaseUrl() { return BASE_URL; }
    public static String getToken() { return token; }
    public static void setToken(String t) { token = t; }
    public static HttpClient getClient() { return client; }
    public static ObjectMapper getMapper() { return mapper; }

    public static boolean isAuthenticated() {
        return token != null && !token.isEmpty();
    }

    /**
     * Clears token.
     */
    public static void clearToken() {
        token = null;
    }

    /**
     * Gets auth header.
     *
     * @return the resulting string
     */
    private static String getAuthHeader() {
        return (token != null && !token.isEmpty()) ? "Bearer " + token : "";
    }

    // ===== GET =====
    /**
     * Gets.
     *
     * @param endpoint API path relative to the base URL
     * @return the raw HTTP response received from the server
     * @throws Exception if the request fails or the server cannot be reached
     */
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
    /**
     * Performs the "post" operation.
     *
     * @param endpoint API path relative to the base URL
     * @param body the request body
     * @return the raw HTTP response received from the server
     * @throws Exception if the request fails or the server cannot be reached
     */
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

    /**
     * Performs the "post" operation.
     *
     * @param endpoint API path relative to the base URL
     * @return the raw HTTP response received from the server
     * @throws Exception if the request fails or the server cannot be reached
     */
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
    /**
     * Performs the "post multipart" operation.
     *
     * @param endpoint API path relative to the base URL
     * @param fields the "fields" value of type {@code Map<String, String>}
     * @param fileFieldName the "file field name" value of type {@code String}
     * @param files the "files" value of type {@code List<File>}
     * @return the raw HTTP response received from the server
     * @throws Exception if the request fails or the server cannot be reached
     */
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
    /**
     * Performs the "put multipart" operation.
     *
     * @param endpoint API path relative to the base URL
     * @param fields the "fields" value of type {@code Map<String, String>}
     * @param removedImageIds the "removed image ids" value of type {@code List<Long>}
     * @param fileFieldName the "file field name" value of type {@code String}
     * @param files the "files" value of type {@code List<File>}
     * @return the raw HTTP response received from the server
     * @throws Exception if the request fails or the server cannot be reached
     */
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
    /**
     * Performs the "put" operation.
     *
     * @param endpoint API path relative to the base URL
     * @param body the request body
     * @return the raw HTTP response received from the server
     * @throws Exception if the request fails or the server cannot be reached
     */
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
    /**
     * Deletes.
     *
     * @param endpoint API path relative to the base URL
     * @return the raw HTTP response received from the server
     * @throws Exception if the request fails or the server cannot be reached
     */
    public static HttpResponse<String> delete(String endpoint) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Authorization", getAuthHeader())
                .DELETE()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // ✅ DELETE با Body (برای delete با payload)
    /**
     * Deletes.
     *
     * @param endpoint API path relative to the base URL
     * @param body the request body
     * @return the raw HTTP response received from the server
     * @throws Exception if the request fails or the server cannot be reached
     */
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

    /**
     * Parses response.
     *
     * @param json the "json" value of type {@code String}
     * @param clazz the "clazz" value of type {@code Class<T>}
     * @return the resulting {@code <T> T} instance
     * @throws Exception if the request fails or the server cannot be reached
     */
    public static <T> T parseResponse(String json, Class<T> clazz) throws Exception {
        return mapper.readValue(json, clazz);
    }

    /**
     * Sends request async.
     *
     * @param endpoint API path relative to the base URL
     * @param method the "method" value of type {@code String}
     * @return a {@code CompletableFuture} that completes asynchronously with the result
     */
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