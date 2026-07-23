package com.secondhand.backend.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object carrying "comment create request" data between client and server.
 * <p>
 * This class is used purely for transferring data between client and server and is not mapped to the database directly, keeping the internal structure of the entities hidden from the client.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class CommentCreateRequest {
    @NotNull(message = "شناسه آگهی الزامی است")
    private Long itemId;

    @NotBlank(message = "متن نظر الزامی است")
    @Size(max = 1000, message = "متن نظر نباید بیشتر از ۱۰۰۰ کاراکتر باشد")
    private String text;

    /**
     * Creates a new {@code CommentCreateRequest} instance.
     */
    public CommentCreateRequest() {}
    public CommentCreateRequest(Long itemId, String text) {
        this.itemId = itemId;
        this.text = text;
    }

    public Long getItemId() { return itemId; }
    public String getText() { return text; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public void setText(String text) { this.text = text; }
}