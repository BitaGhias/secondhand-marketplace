package com.secondhand.backend.dto.item;

public class ItemSearchRequest {
    private String keyword;      // کلمه کلیدی
    private Long categoryId;
    private Long cityId;
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

    public boolean hasCategory() {
        return categoryId != null;
    }

    public boolean hasCity() {
        return cityId != null;
    }

    public boolean hasPriceRange() {
        return (minPrice != null && minPrice > 0) || (maxPrice != null && maxPrice > 0);
    }

    public boolean hasSort() {
        return sortBy != null && !sortBy.trim().isEmpty();
    }
}