package com.secondhand.backend.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CommentCreateRequest {
    @NotNull(message = "شناسه آگهی الزامی است")
    private Long itemId;

    @NotBlank(message = "متن نظر الزامی است")
    @Size(max = 1000, message = "متن نظر نباید بیشتر از ۱۰۰۰ کاراکتر باشد")
    private String text;

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