package com.secondhand.frontend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.model.Rating;

import java.net.http.HttpResponse;

public class RatingService {

    private static final ObjectMapper objectMapper = ApiClient.getMapper();

    // ثبت امتیاز (مسیر درست بک‌اند: POST /api/ratings/add)
    public static Rating rateSeller(Long itemId, int score, String comment) throws Exception {
        RatingRequest request = new RatingRequest(itemId, score, comment);
        HttpResponse<String> response = ApiClient.post("/ratings/add", request);

        if (response.statusCode() == 200 || response.statusCode() == 201) {
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
