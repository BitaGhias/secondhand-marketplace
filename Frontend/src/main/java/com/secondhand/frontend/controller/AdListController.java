package com.secondhand.frontend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.model.Item;
import com.secondhand.frontend.model.User;
import com.secondhand.frontend.service.ApiClient;
import com.secondhand.frontend.service.ItemService;
import com.secondhand.frontend.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class AdListController {

    @FXML private MenuButton userMenuButton;
    @FXML private TextField searchField;
    @FXML private FlowPane adsFlowPane;
    @FXML private VBox loadingContainer;

    @FXML
    public void initialize() {
        // تنظیم نام کاربر از SessionManager
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null && userMenuButton != null) {
            userMenuButton.setText("👤 " + currentUser.getFullName());
        }

        setupMenuActions();
        fetchAdsFromBackend();

        // جستجوی لحظه‌ای
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                handleSearch(newValue);
            });
        }
    }

    private void fetchAdsFromBackend() {
        try {
            List<Item> items = ItemService.getActiveItems();

            Platform.runLater(() -> {
                if (loadingContainer != null) {
                    loadingContainer.setVisible(false);
                }

                if (adsFlowPane != null) {
                    adsFlowPane.getChildren().clear();

                    if (items == null || items.isEmpty()) {
                        Label emptyLabel = new Label("هیچ آگهی فعالی وجود ندارد");
                        emptyLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #888;");
                        adsFlowPane.getChildren().add(emptyLabel);
                        return;
                    }

                    for (Item item : items) {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/secondhand/frontend/item_ad.fxml"));
                            Parent card = loader.load();

                            ItemAdController controller = loader.getController();
                            controller.setItem(item);

                            // کلیک روی کارت -> رفتن به صفحه جزئیات
                            card.setOnMouseClicked(event -> {
                                goToItemDetail(item);
                            });

                            adsFlowPane.getChildren().add(card);

                        } catch (Exception e) {
                            System.err.println("❌ خطا در رندر کارت آگهی:");
                            e.printStackTrace();
                        }
                    }
                }
            });

        } catch (Exception e) {
            System.err.println("❌ خطا در دریافت آگهی‌ها:");
            e.printStackTrace();
            Platform.runLater(() -> {
                if (loadingContainer != null) {
                    loadingContainer.setVisible(false);
                }
                Label errorLabel = new Label("خطا در بارگذاری آگهی‌ها: " + e.getMessage());
                errorLabel.setStyle("-fx-text-fill: #ff576c; -fx-font-size: 14px;");
                if (adsFlowPane != null) {
                    adsFlowPane.getChildren().add(errorLabel);
                }
            });
        }
    }

    private void goToItemDetail(Item item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/secondhand/frontend/item_detail.fxml"));
            Parent root = loader.load();

            ItemDetailController controller = loader.getController();
            controller.setItem(item);

            // تغییر صحنه
            Stage stage = (Stage) adsFlowPane.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("جزئیات آگهی");

        } catch (Exception e) {
            System.err.println("❌ خطا در رفتن به صفحه جزئیات:");
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
                            goToMyAds();
                            break;
                        case "❤️ علاقه‌مندی‌ها":
                            goToFavorites();
                            break;
                        case "💬 گفت‌وگوها":
                            goToChats();
                            break;
                        case "🛒 خریدها":
                            goToPurchases();
                            break;
                        case "ثبت آگهی جدید":
                            goToCreateAd();
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
        // TODO: پیاده‌سازی جستجو
        System.out.println("🔍 جستجو: " + query);
    }

    @FXML
    private void goToMyAds() {
        try {
            MainApplication.changeScene("/com/secondhand/frontend/my_ads.fxml", "آگهی‌های من");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToFavorites() {
        try {
            MainApplication.changeScene("/com/secondhand/frontend/favorites.fxml", "علاقه‌مندی‌ها");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToChats() {
        try {
            MainApplication.changeScene("/com/secondhand/frontend/chats.fxml", "پیام‌ها");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToPurchases() {
        try {
            MainApplication.changeScene("/com/secondhand/frontend/purchases.fxml", "خریدها");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToCreateAd() {
        try {
            MainApplication.changeScene("/com/secondhand/frontend/create_ad.fxml", "ثبت آگهی جدید");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.logout();
        try {
            MainApplication.changeScene("/com/secondhand/frontend/login.fxml", "ورود");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}