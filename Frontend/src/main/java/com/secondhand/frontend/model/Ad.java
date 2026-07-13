package com.secondhand.frontend.model;

import java.util.Map;

public class Ad {
    private Long id;
    private String title;
    private String description;
    private Double price;
    private String status;
    private Map<String, Object> user; // دریافت شیء کاربر به صورت Map ساده
    private Map<String, Object> city; // دریافت شیء شهر به صورت Map ساده

    public Ad() {}

    // متدهای کمکی میانبر برای استخراج راحت‌تر داده‌ها در فرانت
    public String getUsername() {
        if (user != null && user.containsKey("fullName")) {
            return (String) user.get("fullName");
        }
        return "کاربر ناشناس";
    }

    public String getCityName() {
        if (city != null && city.containsKey("name")) {
            return (String) city.get("name");
        }
        return "نامشخص";
    }

    public String getFormattedPrice() {
        if (price == null || price == 0) {
            return "توافقی";
        }
        return String.format("%,.0f تومان", price);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Map<String, Object> getUser() { return user; }
    public void setUser(Map<String, Object> user) { this.user = user; }

    public Map<String, Object> getCity() { return city; }
    public void setCity(Map<String, Object> city) { this.city = city; }
}