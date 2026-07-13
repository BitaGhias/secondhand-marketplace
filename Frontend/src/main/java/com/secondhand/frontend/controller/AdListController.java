package com.secondhand.frontend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.model.Ad;
import com.secondhand.frontend.service.ApiClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class AdListController {

    @FXML
    private MenuButton userMenuButton;

    @FXML
    private TextField searchField;

    // 🟢 متصل به کامپوننت جدید در FXML برای چیدن کارت‌های شیشه‌ای آگهی
    @FXML
    private FlowPane adsFlowPane;

    @FXML
    public void initialize() {
        // ۱. ست کردن اکشن روی آیتم‌های منوی کاربر
        setupMenuActions();

        // ۲. گوش دادن به تغییرات فیلد جستجو به صورت لحظه‌ای
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                handleSearch(newValue);
            });
        }

        // ۳. گرفتن آگهی‌های واقعی از دیتابیس بک‌اَند و رندر کردن کارت‌ها
        fetchAdsFromBackend();
    }

    /**
     * متد کمکی برای ست کردن نام کاربر به صورت داینامیک از طریق دیتای دریافتی لاگین
     */
    public void setUserProfile(String fullName) {
        if (userMenuButton != null && fullName != null && !fullName.isEmpty()) {
            Platform.runLater(() -> userMenuButton.setText("👤 " + fullName));
        }
    }

    /**
     * ارتباط مستقیم با API بک‌اَند برای دریافت آیتم‌ها
     */
    private void fetchAdsFromBackend() {
        try {
            String targetUrl = ApiClient.getBaseUrl() + "/items/approved";
            System.out.println("🔗 Sending request to: " + targetUrl);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Authorization", "Bearer " + ApiClient.getToken()) // ارسال توکن JWT دریافت شده در لاگین
                    .GET()
                    .build();

            ApiClient.getClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(this::handleAdsResponse)
                    .exceptionally(e -> {
                        System.err.println("❌ خطا در دریافت اطلاعات از بک‌اَند:");
                        e.printStackTrace();
                        return null;
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * پردازش پاسخ بک‌اَند و رندر کردن پویای هر کارت آگهی
     */
    private void handleAdsResponse(String jsonResponse) {
        // 🔴 چاپ خروجی خام بک‌اَند در کنسول برای عیب‌یابی دقیق‌تر ساختار دیتای شما
        System.out.println("🟢 DEBUG - JSON Response from Backend: " + jsonResponse);

        try {
            ObjectMapper mapper = ApiClient.getMapper();
            List<Ad> ads;

            // خوندن خروجی به صورت نودهای درختی جکسون
            JsonNode rootNode = mapper.readTree(jsonResponse);

            if (rootNode.isArray()) {
                // سناریو اول: بک‌اَند مستقیماً یک آرایه JSON فرستاده است [{}, {}]
                ads = mapper.readerFor(new TypeReference<List<Ad>>() {}).readValue(rootNode);
            } else if (rootNode.isObject() && rootNode.has("content")) {
                // سناریو دوم: خروجی بک‌اَند صفحه‌بندی شده (Pageable) است و آرایه آگهی‌ها داخل فیلد content قرار دارد
                ads = mapper.readerFor(new TypeReference<List<Ad>>() {}).readValue(rootNode.get("content"));
            } else {
                // سناریو سوم: تلاش مستقیم در صورتی که دیتای بالا وجود نداشت
                ads = mapper.readValue(jsonResponse, new TypeReference<List<Ad>>() {});
            }

            Platform.runLater(() -> {
                if (adsFlowPane != null) {
                    adsFlowPane.getChildren().clear(); // پاک کردن لودینگ‌ها یا دیتای قبلی

                    for (Ad ad : ads) {
                        try {
                            // بارگذاری فایل کارت تکی
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/secondhand/frontend/item_ad.fxml"));
                            Parent card = loader.load();

                            // پر کردن اطلاعات کارت از طریق کنترلر اختصاصی آن
                            ItemAdController itemController = loader.getController();
                            itemController.setData(ad);

                            // اضافه کردن کارت نئونی به صفحه گرید اصلی
                            adsFlowPane.getChildren().add(card);

                        } catch (Exception e) {
                            System.err.println("❌ خطا در رندر کارت آگهی انفرادی:");
                            e.printStackTrace();
                        }
                    }
                }
            });

        } catch (Exception e) {
            System.err.println("❌ خطا در پارس کردن دیتای آگهی‌ها:");
            e.printStackTrace();
        }
    }

    private void setupMenuActions() {
        if (userMenuButton != null && userMenuButton.getItems() != null) {
            for (MenuItem item : userMenuButton.getItems()) {
                item.setOnAction(event -> {
                    String itemText = item.getText();
                    if (itemText == null) return;

                    switch (itemText.trim()) {
                        case "📝 آگهی‌های من":
                            System.out.println("تغییر مسیر به آگهی‌های من...");
                            break;
                        case "❤️ علاقه‌مندی‌ها":
                            System.out.println("باز کردن علاقه‌مندی‌ها...");
                            break;
                        case "💬 گفت‌وگوها":
                            System.out.println("باز کردن چت‌ها...");
                            break;
                        case "🛒 خریدها":
                            System.out.println("باز کردن تاریخچه خرید...");
                            break;
                        case "خروج":
                        case "🚪 خروج":
                            handleLogout();
                            break;
                    }
                });
            }
        }
    }

    private void handleSearch(String query) {
        System.out.println("در حال جستجو برای: " + query);
        // اینجا در آینده منطق فیلتر کردن داینامیک گرید بر اساس متن وارد شده قرار می‌گیرد.
    }

    private void handleLogout() {
        try {
            System.out.println("در حال خروج از حساب کاربری...");
            MainApplication.changeScene("/com/secondhand/frontend/login.fxml", "فروشگاه دست دوم - ورود");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}