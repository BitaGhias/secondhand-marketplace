package com.secondhand.frontend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.model.City;
import com.secondhand.frontend.util.ApiClient;

import java.net.http.HttpResponse;
import java.util.List;

public class CityService {

    // استفاده از مپر مشترک (FAIL_ON_UNKNOWN_PROPERTIES غیرفعال) تا فیلد اضافی پاسخ سرور باعث خطا نشود
    private static final ObjectMapper objectMapper = ApiClient.getMapper();

    // دریافت همه شهرها
    public static List<City> getAllCities() throws Exception {
        HttpResponse<String> response = ApiClient.get("/cities");

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<City>>() {});
        } else {
            throw new Exception("خطا در دریافت شهرها: " + response.body());
        }
    }
}