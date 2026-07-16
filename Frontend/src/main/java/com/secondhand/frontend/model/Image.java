package com.secondhand.frontend.model;

public class Image {
    private Long id;
    private String imagePath;

    public Image() {}

    public Image(Long id, String imagePath) {
        this.id = id;
        this.imagePath = imagePath;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    // ===== متدهای کمکی =====

    public String getFullUrl() {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }
        if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
            return imagePath;
        }
        if (imagePath.startsWith("/")) {
            return "http://localhost:8080" + imagePath;
        }
        return "http://localhost:8080/uploads/" + imagePath;
    }

    public boolean isValid() {
        return imagePath != null && !imagePath.trim().isEmpty();
    }

    public String getFileName() {
        if (imagePath == null) return null;
        int lastSlash = imagePath.lastIndexOf('/');
        if (lastSlash != -1 && lastSlash < imagePath.length() - 1) {
            return imagePath.substring(lastSlash + 1);
        }
        return imagePath;
    }
}