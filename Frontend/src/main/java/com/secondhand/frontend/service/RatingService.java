package com.secondhand.frontend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.model.Rating;
import com.secondhand.frontend.service.ApiClient;

import java.net.http.HttpResponse;

public class RatingService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // ثبت امتیاز
    public static Rating rateSeller(Long itemId, int score, String comment) throws Exception {
        RatingRequest request = new RatingRequest(itemId, score, comment);
        HttpResponse<String> response = ApiClient.post("/ratings", request);

        if (response.statusCode() == 201) {
            return objectMapper.readValue(response.body(), Rating.class);
        } else {
            throw new Exception("خطا در ثبت امتیاز: " + response.body());
        }
    }

    public static class RatingRequest {
        public Long itemId;
        public int score;
        public String comment;
        public RatingRequest(Long itemId, int score, String comment) {
            this.itemId = itemId;
            this.score = score;
            this.comment = comment;
        }
    }
}