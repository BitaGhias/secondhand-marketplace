package com.secondhand.frontend.model;

/**
 * Client-side model representing "category" data returned by the server.
 * <p>
 * This class is the client-side representation of data received from the server and is deserialized from JSON by Jackson.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class Category {
    private Long id;
    private String name;
    private String parentCategoryName;
    private Long parentId;
    private String parentName;
    private Boolean hasChildren;
    private Long itemCount;

    /**
     * Creates a new {@code Category} instance.
     */
    public Category() {}

    public Category(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters & Setters
    /**
     * Gets id.
     *
     * @return the resulting numeric value
     */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getParentCategoryName() { return parentCategoryName; }
    public void setParentCategoryName(String parentCategoryName) { this.parentCategoryName = parentCategoryName; }

    @Override
    public String toString() {
        return name != null ? name : "";
    }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }

    public Boolean getHasChildren() { return hasChildren; }
    public void setHasChildren(Boolean hasChildren) { this.hasChildren = hasChildren; }

    public Long getItemCount() { return itemCount; }
    public void setItemCount(Long itemCount) { this.itemCount = itemCount; }

    public boolean isRoot() { return parentId == null; }
}
