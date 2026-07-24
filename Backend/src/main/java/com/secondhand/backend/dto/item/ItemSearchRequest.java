package com.secondhand.backend.dto.item;

/**
 * Data Transfer Object carrying "item search request" data between client and server.
 * <p>
 * This class is used purely for transferring data between client and server and is not mapped to the database directly, keeping the internal structure of the entities hidden from the client.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class ItemSearchRequest {
    private String keyword;      // کلمه کلیدی
    private Long categoryId;
    private Long cityId;
    /**
     * Creates a new {@code ItemSearchRequest} instance.
     */
    private Long minPrice;     // حداقل قیمت
    private Long maxPrice;     // حداکثر قیمت
    private String sortBy;       // newest, oldest, price_asc, price_desc

    public ItemSearchRequest() {}

    public ItemSearchRequest(String keyword, Long categoryId, Long cityId,
                             Long minPrice, Long maxPrice, String sortBy) {
        this.keyword = keyword;
        this.categoryId = categoryId;
        this.cityId = cityId;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.sortBy = sortBy;
    }

    public String getKeyword() { return keyword; }
    public Long getCategoryId() { return categoryId; }
    public Long getCityId() { return cityId; }
    /**
     * Gets min price.
     *
     * @return the resulting numeric value
     */
    public Long getMinPrice() { return minPrice; }
    public Long getMaxPrice() { return maxPrice; }
    public String getSortBy() { return sortBy; }

    public void setKeyword(String keyword) { this.keyword = keyword; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public void setCityId(Long cityId) { this.cityId = cityId; }
    public void setMinPrice(Long minPrice) { this.minPrice = minPrice; }
    public void setMaxPrice(Long maxPrice) { this.maxPrice = maxPrice; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }

    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }

    /**
     * Checks whether the "category" condition holds.
     *
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     */
    public boolean hasCategory() {
        return categoryId != null;
    }

    /**
     * Checks whether the "city" condition holds.
     *
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     */
    public boolean hasCity() {
        return cityId != null;
    }

    /**
     * Checks whether the "price range" condition holds.
     *
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     */
    public boolean hasPriceRange() {
        return (minPrice != null && minPrice > 0) || (maxPrice != null && maxPrice > 0);
    }

    /**
     * Checks whether the "sort" condition holds.
     *
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     */
    public boolean hasSort() {
        return sortBy != null && !sortBy.trim().isEmpty();
    }
}