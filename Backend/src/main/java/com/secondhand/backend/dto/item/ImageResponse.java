package com.secondhand.backend.dto.item;

/**
 * Data Transfer Object carrying "image response" data between client and server.
 * <p>
 * This class is used purely for transferring data between client and server and is not mapped to the database directly, keeping the internal structure of the entities hidden from the client.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class ImageResponse {
    private Long id;
    private String imagePath;

    /**
     * Creates a new {@code ImageResponse} instance.
     */
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