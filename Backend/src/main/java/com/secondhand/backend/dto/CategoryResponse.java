package com.secondhand.backend.dto;

public class CategoryResponse {
    private Long id;
    private String name;
    private Long parentId;
    private String parentName;
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