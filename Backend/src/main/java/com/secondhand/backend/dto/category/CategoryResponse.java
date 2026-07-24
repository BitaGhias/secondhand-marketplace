package com.secondhand.backend.dto.category;

/**
 * Data Transfer Object carrying "category response" data between client and server.
 * <p>
 * This class is used purely for transferring data between client and server and is not mapped to the database directly, keeping the internal structure of the entities hidden from the client.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class CategoryResponse {
    private Long id;
    private String name;
    private Long parentId;
    private String parentName;
    /**
     * Creates a new {@code CategoryResponse} instance.
     */
    private Long itemCount;  // تعداد آگهی‌های فعال در این دسته‌بندی
    private boolean hasChildren;  // آیا زیردسته دارد؟

    public CategoryResponse() {}

    public CategoryResponse(Long id, String name, Long parentId, String parentName, Long itemCount, boolean hasChildren) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.parentName = parentName;
        this.itemCount = itemCount;
        this.hasChildren = hasChildren;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Long getParentId() { return parentId; }
    public String getParentName() { return parentName; }
    public Long getItemCount() { return itemCount; }
    public boolean isHasChildren() { return hasChildren; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public void setParentName(String parentName) { this.parentName = parentName; }
    public void setItemCount(Long itemCount) { this.itemCount = itemCount; }
    public void setHasChildren(boolean hasChildren) { this.hasChildren = hasChildren; }
}