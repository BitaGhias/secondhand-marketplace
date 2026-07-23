package com.secondhand.backend.dto.rating;

/**
 * Data Transfer Object carrying "rating response" data between client and server.
 * <p>
 * This class is used purely for transferring data between client and server and is not mapped to the database directly, keeping the internal structure of the entities hidden from the client.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class RatingResponse {
    private Long id;
    private int score;
    private String comment;
    private Long itemId;
    private String itemTitle;
    private Long raterId;
    private String raterUsername;
    private Long sellerId;
    private String sellerUsername;

    /**
     * Creates a new {@code RatingResponse} instance.
     */
    public RatingResponse() {}

    public RatingResponse(Long id, int score, String comment, Long itemId, String itemTitle,
                          Long raterId, String raterUsername, Long sellerId, String sellerUsername) {
        this.id = id;
        this.score = score;
        this.comment = comment;
        this.itemId = itemId;
        this.itemTitle = itemTitle;
        this.raterId = raterId;
        this.raterUsername = raterUsername;
        this.sellerId = sellerId;
        this.sellerUsername = sellerUsername;
    }

    public Long getId() { return id; }
    public int getScore() { return score; }
    public String getComment() { return comment; }
    public Long getItemId() { return itemId; }
    public String getItemTitle() { return itemTitle; }
    public Long getRaterId() { return raterId; }
    public String getRaterUsername() { return raterUsername; }
    public Long getSellerId() { return sellerId; }
    public String getSellerUsername() { return sellerUsername; }

    public void setId(Long id) { this.id = id; }
    public void setScore(int score) { this.score = score; }
    public void setComment(String comment) { this.comment = comment; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public void setItemTitle(String itemTitle) { this.itemTitle = itemTitle; }
    public void setRaterId(Long raterId) { this.raterId = raterId; }
    public void setRaterUsername(String raterUsername) { this.raterUsername = raterUsername; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }
    public void setSellerUsername(String sellerUsername) { this.sellerUsername = sellerUsername; }
}