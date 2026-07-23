package com.secondhand.backend.dto.favorite;

import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object carrying "favorite request" data between client and server.
 * <p>
 * This class is used purely for transferring data between client and server and is not mapped to the database directly, keeping the internal structure of the entities hidden from the client.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class FavoriteRequest {
    @NotNull(message = "شناسه آگهی الزامی است")
    private Long itemId;

    public FavoriteRequest() {}
    public FavoriteRequest(Long itemId) { this.itemId = itemId; }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
}