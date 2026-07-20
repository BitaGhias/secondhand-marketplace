package com.secondhand.backend.dto.item;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public class ItemCreateRequest {
    private String title;
    private String description;
    private Long price;
    private Long categoryId;
    private Long cityId;
    private List<MultipartFile> images;

    public ItemCreateRequest() {}

    public ItemCreateRequest(String title, String description, Long price,
                             Long categoryId, Long cityId, List<MultipartFile> images) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.categoryId = categoryId;
        this.cityId = cityId;
        this.images = images;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Long getPrice() { return price; }
    public Long getCategoryId() { return categoryId; }
    public Long getCityId() { return cityId; }
    public List<MultipartFile> getImages() { return images; }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(Long price) { this.price = price; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public void setCityId(Long cityId) { this.cityId = cityId; }
    public void setImages(List<MultipartFile> images) { this.images = images; }
}