package com.secondhand.frontend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Client-side model representing "image" data returned by the server.
 * <p>
 * This class is the client-side representation of data received from the server and is deserialized from JSON by Jackson.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Image {
    private Long id;
    private String imagePath;

    /**
     * Creates a new {@code Image} instance.
     */
    public Image() {}

    public Image(Long id, String imagePath) {
        this.id = id;
        this.imagePath = imagePath;
    }

    /**
     * Gets id.
     *
     * @return the resulting numeric value
     */
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
        // بک‌اند مسیر را به صورت "uploads/123.jpg" (یا با \ در ویندوز) ذخیره می‌کند؛
        // فقط نام فایل را برمی‌داریم و به مسیر استاتیک /uploads/ می‌چسبانیم
        return "http://localhost:8080/uploads/" + getFileName();
    }

    /**
     * Checks whether the "valid" condition holds.
     *
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     */
    public boolean isValid() {
        return imagePath != null && !imagePath.trim().isEmpty();
    }

    /**
     * Gets file name.
     *
     * @return the resulting string
     */
    public String getFileName() {
        if (imagePath == null) return null;
        String normalized = imagePath.replace('\\', '/');
        int lastSlash = normalized.lastIndexOf('/');
        if (lastSlash != -1 && lastSlash < normalized.length() - 1) {
            return normalized.substring(lastSlash + 1);
        }
        return normalized;
    }
}
