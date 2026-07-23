package com.secondhand.backend.dto.rating;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object carrying "rating create request" data between client and server.
 * <p>
 * This class is used purely for transferring data between client and server and is not mapped to the database directly, keeping the internal structure of the entities hidden from the client.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class RatingCreateRequest {
    @NotNull(message = "شناسه آگهی الزامی است")
    private Long itemId;

    @Min(value = 1, message = "امتیاز حداقل باید ۱ باشد")
    @Max(value = 5, message = "امتیاز حداکثر باید ۵ باشد")
    private int score;

    @Size(max = 1000, message = "متن امتیاز نباید بیشتر از ۱۰۰۰ کاراکتر باشد")
    private String comment;

    /**
     * Creates a new {@code RatingCreateRequest} instance.
     */
    public RatingCreateRequest() {}
    public RatingCreateRequest(Long itemId, int score, String comment) {
        this.itemId = itemId;
        this.score = score;
        this.comment = comment;
    }

    public Long getItemId() { return itemId; }
    public int getScore() { return score; }
    public String getComment() { return comment; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public void setScore(int score) { this.score = score; }
    public void setComment(String comment) { this.comment = comment; }
}