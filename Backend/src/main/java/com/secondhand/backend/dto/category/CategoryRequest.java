package com.secondhand.backend.dto.category;

/**
 * Data Transfer Object carrying "category request" data between client and server.
 * <p>
 * This class is used purely for transferring data between client and server and is not mapped to the database directly, keeping the internal structure of the entities hidden from the client.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class CategoryRequest {
    private String name;
    /**
     * Creates a new {@code CategoryRequest} instance.
     */
    private Long parentId;  // برای ایجاد زیردسته

    public CategoryRequest() {}

    public CategoryRequest(String name, Long parentId) {
        this.name = name;
        this.parentId = parentId;
    }

    public String getName() { return name; }
    public Long getParentId() { return parentId; }

    public void setName(String name) { this.name = name; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
}