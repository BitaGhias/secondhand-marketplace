package com.secondhand.frontend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.model.User;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;

public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))//اگه 10 ثانیه جواب نیومد قطع کن
            .build();
    private static final ObjectMapper mapper = new ObjectMapper();//برای تبدیل json به شی java

    private static String token = null;

    public static String getBaseUrl() { return BASE_URL; }
    public static String getToken() { return token; }
    public static void setToken(String t) { token = t; }
    public static HttpClient getClient() { return client; }
    public static ObjectMapper getMapper() { return mapper; }
}