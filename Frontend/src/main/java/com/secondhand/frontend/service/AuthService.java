package com.secondhand.frontend.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class AuthService
{
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String BASE_URL = "http://localhost:8080/api/auth";

    public boolean register(String fullName, String username, String password) {
        try {
            String jsonInput = "{" +
                    "\"fullName\":\"" + fullName + "\"," +
                    "\"username\":\"" + username + "\"," +
                    "\"password\":\"" + password + "\"," +
                    "\"active\":true" +
                    "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/register"))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    // اعمال فرمت UTF-8 برای پشتیبانی از زبان فارسی
                    .POST(HttpRequest.BodyPublishers.ofString(jsonInput, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Server Response Status Code: " + response.statusCode());
            System.out.println("Server Response Body: " + response.body());

            return response.statusCode() == 200 || response.statusCode() == 201;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean login(String username, String password) {
        try {
            // ساخت رشته استاندارد JSON برای لاگین
            String jsonInput = "{" +
                    "\"username\":\"" + username + "\"," +
                    "\"password\":\"" + password + "\"" +
                    "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/login"))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonInput, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // اضافه کردن لاگ برای اینکه ببینیم سرور دقیقاً چه کدی برمی‌گرداند
            System.out.println("--- LOGIN RESPONSE ---");
            System.out.println("Status Code: " + response.statusCode());
            System.out.println("Response Body: " + response.body());

            return response.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("خطا در ارسال درخواست لاگین لایه شبکه:");
            e.printStackTrace();
            return false;
        }
    }
}