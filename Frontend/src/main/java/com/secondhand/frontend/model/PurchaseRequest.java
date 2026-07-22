package com.secondhand.frontend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** مدل درخواست خرید — مطابق PurchaseRequestResponse بک‌اند */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PurchaseRequest {
    private Long id;
    private Long itemId;
    private String itemTitle;
    private Long buyerId;
    private String buyerUsername;
    private String buyerFullName;
    private String buyerPhone;
    private String buyerEmail;
    private Long sellerId;
    private String sellerUsername;
    private String status;
    private String createdAt;
    private String respondedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public String getItemTitle() { return itemTitle; }
    public void setItemTitle(String itemTitle) { this.itemTitle = itemTitle; }
    public Long getBuyerId() { return buyerId; }
    public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }
    public String getBuyerUsername() { return buyerUsername; }
    public void setBuyerUsername(String buyerUsername) { this.buyerUsername = buyerUsername; }
    public String getBuyerFullName() { return buyerFullName; }
    public void setBuyerFullName(String buyerFullName) { this.buyerFullName = buyerFullName; }
    public String getBuyerPhone() { return buyerPhone; }
    public void setBuyerPhone(String buyerPhone) { this.buyerPhone = buyerPhone; }
    public String getBuyerEmail() { return buyerEmail; }
    public void setBuyerEmail(String buyerEmail) { this.buyerEmail = buyerEmail; }
    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }
    public String getSellerUsername() { return sellerUsername; }
    public void setSellerUsername(String sellerUsername) { this.sellerUsername = sellerUsername; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getRespondedAt() { return respondedAt; }
    public void setRespondedAt(String respondedAt) { this.respondedAt = respondedAt; }

    public boolean isPending()  { return "PENDING".equalsIgnoreCase(status); }
    public boolean isAccepted() { return "ACCEPTED".equalsIgnoreCase(status); }
    public boolean isDeclined() { return "DECLINED".equalsIgnoreCase(status); }

    public String getPersianStatus() {
        if (isAccepted()) return "تایید شد";
        if (isDeclined()) return "رد شد";
        return "در انتظار";
    }

    public String getShortDate() {
        if (createdAt == null) return "";
        return createdAt.replace("T", " — ").length() > 21 ? createdAt.replace("T", " — ").substring(0, 21) : createdAt.replace("T", " — ");
    }
}
