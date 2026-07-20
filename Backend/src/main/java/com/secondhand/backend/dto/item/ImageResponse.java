package com.secondhand.backend.dto.item;

public class ImageResponse {
    private Long id;
    private String imagePath;

    public ImageResponse() {}

    public ImageResponse(Long id, String imagePath) {
        this.id = id;
        this.imagePath = imagePath;
    }

    public Long getId() { return id; }
    public String getImagePath() { return imagePath; }

    public void setId(Long id) { this.id = id; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
}