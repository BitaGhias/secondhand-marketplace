package com.secondhand.frontend.controller;

import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.model.Item;
import com.secondhand.frontend.model.User;
import com.secondhand.frontend.service.ItemService;
import com.secondhand.frontend.util.SessionManager;
import com.secondhand.frontend.util.WindowUtil;
import javafx.animation.PauseTransition;
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
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

public class AdListController extends BaseController implements FilterDialogueController.FilterListener {

    @FXML private MenuButton userMenuButton;
    @FXML private TextField searchField;
    @FXML private FlowPane adsFlowPane;
    @FXML private VBox loadingContainer;
    @FXML private HBox titleBar;

    // جست‌وجوی لحظه‌ای با تاخیر کوتاه (debounce) تا برای هر حرف درخواست نفرستیم
    private final PauseTransition searchDebounce = new PauseTransition(Duration.millis(400));

    // فیلترهای فعال
    private Long filterCategoryId;
    private Long filterCityId;
    private Integer filterMinPrice;
    private Integer filterMaxPrice;

    @FXML
    public void initialize() {
        WindowUtil.makeDraggable(titleBar);
        // تنظیم نام کاربر از SessionManager
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null && userMenuButton != null) {
            userMenuButton.setText("👤 " + currentUser.getFullName());
        }

        setupMenuActions();

        // دسترسی سریع ادمین به پنل مدیریت از داخل لیست آگهی‌ها
        if (SessionManager.isAdmin() && userMenuButton != null) {
            MenuItem adminPanelItem = new MenuItem("🛡️ پنل مدیریت");
            adminPanelItem.setOnAction(e -> goToAdminPanel());
            userMenuButton.getItems().add(0, adminPanelItem);
        }

        fetchAdsFromBackend();

        // جستجوی لحظه‌ای
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                searchDebounce.setOnFinished(e -> runFilteredSearch());
                searchDebounce.playFromStart();
            });
        }
    }

    private void fetchAdsFromBackend() {
        new Thread(() -> {
            try {
                List<Item> items = ItemService.getActiveItems();
                Platform.runLater(() -> renderItems(items));
            } catch (Exception e) {
                showLoadError(e);
            }
        }).start();
    }

    @FXML
    private void handleSearchClick() {
        runFilteredSearch();
    }

    private boolean hasActiveFilterOrSearch() {
        String keyword = searchField != null ? searchField.getText() : null;
        return (keyword != null && !keyword.isBlank())
                || filterCategoryId != null || filterCityId != null
                || filterMinPrice != null || filterMaxPrice != null;
    }

    private void runFilteredSearch() {
        if (!hasActiveFilterOrSearch()) {
            fetchAdsFromBackend();
            return;
        }

        final String keyword = searchField != null ? searchField.getText() : null;
        new Thread(() -> {
            try {
                List<Item> items = ItemService.searchItems(
                        keyword, filterCategoryId, filterCityId, filterMinPrice, filterMaxPrice);
                Platform.runLater(() -> renderItems(items));
            } catch (Exception e) {
                showLoadError(e);
            }
        }).start();
    }

    private void renderItems(List<Item> items) {
        if (loadingContainer != null) {
            loadingContainer.setVisible(false);
        }
        if (adsFlowPane == null) return;

        adsFlowPane.getChildren().clear();

        if (items == null || items.isEmpty()) {
            Label emptyLabel = new Label("هیچ آگهی‌ای برای نمایش وجود ندارد");
            emptyLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: rgba(255,255,255,0.45);");
            // نمایش پیام در مرکز صفحه
            StackPane emptyPane = new StackPane(emptyLabel);
            emptyPane.setAlignment(Pos.CENTER);
            emptyPane.prefWidthProperty().bind(adsFlowPane.widthProperty());
            emptyPane.setPrefHeight(420);
            adsFlowPane.getChildren().add(emptyPane);
            return;
        }

        for (Item item : items) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/secondhand/frontend/item_ad.fxml"));
                Parent card = loader.load();

                ItemAdController controller = loader.getController();
                controller.setItem(item);

                // کلیک روی کارت -> رفتن به صفحه جزئیات
                card.setOnMouseClicked(event -> goToItemDetail(item));

                adsFlowPane.getChildren().add(card);
            } catch (Exception e) {
                System.err.println("❌ خطا در رندر کارت آگهی:");
                e.printStackTrace();
            }
        }
    }

    private void showLoadError(Exception e) {
        System.err.println("❌ خطا در دریافت آگهی‌ها:");
        e.printStackTrace();
        Platform.runLater(() -> {
            if (loadingContainer != null) {
                loadingContainer.setVisible(false);
            }
            if (adsFlowPane != null) {
                adsFlowPane.getChildren().clear();
                Label errorLabel = new Label("خطا در بارگذاری آگهی‌ها: " + e.getMessage());
                errorLabel.setStyle("-fx-text-fill: #ff576c; -fx-font-size: 14px;");
                adsFlowPane.getChildren().add(errorLabel);
            }
        });
    }

    private void goToItemDetail(Item item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/secondhand/frontend/item_detail.fxml"));
            Parent root = loader.load();

            ItemDetailController controller = loader.getController();
            controller.setItem(item);

            Stage stage = (Stage) adsFlowPane.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 1000);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            scene.getStylesheets().add(getClass().getResource("/com/secondhand/frontend/css/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("جزئیات آگهی");
        } catch (Exception e) {
            System.err.println("❌ خطا در رفتن به صفحه جزئیات:");
            e.printStackTrace();
        }
    }

    // باز کردن دیالوگ فیلترهای پیشرفته
    @FXML
    private void showFilterDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/secondhand/frontend/filter_dialogue.fxml"));
            Parent root = loader.load();

            FilterDialogueController controller = loader.getController();
            controller.setListener(this);

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(adsFlowPane.getScene().getWindow());
            dialog.setTitle("فیلترهای جست‌وجو");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFilterApplied(Long categoryId, Long cityId, Integer minPrice, Integer maxPrice) {
        this.filterCategoryId = categoryId;
        this.filterCityId = cityId;
        this.filterMinPrice = minPrice;
        this.filterMaxPrice = maxPrice;
        runFilteredSearch();
    }

    @Override
    public void onFilterCleared() {
        this.filterCategoryId = null;
        this.filterCityId = null;
        this.filterMinPrice = null;
        this.filterMaxPrice = null;
        runFilteredSearch();
    }

    private void setupMenuActions() {
        if (userMenuButton != null && userMenuButton.getItems() != null) {
            for (MenuItem item : userMenuButton.getItems()) {
                item.setOnAction(event -> {
                    String itemText = item.getText();
                    if (itemText == null) return;

                    switch (itemText.trim()) {
                        case "➕ ثبت آگهی جدید":
                        case "ثبت آگهی جدید":
                            goToCreateAd();
                            break;
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
                        case "👤 پروفایل":
                            goToProfile();
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
    private void goToAdminPanel() {
        try {
            MainApplication.changeScene("/com/secondhand/frontend/admin_panel.fxml", "پنل مدیریت");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToProfile() {
        try {
            MainApplication.changeScene("/com/secondhand/frontend/profile.fxml", "پروفایل من");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleLogout() {
        SessionManager.logout();
        try {
            MainApplication.changeScene("/com/secondhand/frontend/login.fxml", "ورود");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
