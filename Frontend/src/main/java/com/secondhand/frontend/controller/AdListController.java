package com.secondhand.frontend.controller;

import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.model.Item;
import com.secondhand.frontend.model.User;
import com.secondhand.frontend.service.ItemService;
import com.secondhand.frontend.util.Routes;
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

public class AdListController extends BaseController implements FilterDialogController.FilterListener {

    @FXML private MenuButton userMenuButton;
    @FXML private TextField  searchField;
    @FXML private FlowPane   adsFlowPane;
    @FXML private VBox       loadingContainer;
    @FXML private HBox       titleBar;

    private final PauseTransition searchDebounce = new PauseTransition(Duration.millis(400));

    private Long filterCategoryId;
    private Long filterCityId;
    private Long filterMinPrice;
    private Long filterMaxPrice;
    private String filterSortBy = "newest";

    @FXML
    public void initialize() {
        WindowUtil.makeDraggable(titleBar);
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null && userMenuButton != null) {
            userMenuButton.setText("👤 " + currentUser.getFullName());
        }

        setupMenuActions();

        if (SessionManager.isAdmin() && userMenuButton != null) {
            MenuItem adminPanelItem = new MenuItem("🛡️ پنل مدیریت");
            adminPanelItem.setOnAction(e -> goToAdminPanel());
            userMenuButton.getItems().add(0, adminPanelItem);
        }

        fetchAdsFromBackend();

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
    private void handleSearchClick() { runFilteredSearch(); }

    private boolean hasActiveFilterOrSearch() {
        String keyword = searchField != null ? searchField.getText() : null;
        return (keyword != null && !keyword.isBlank())
                || filterCategoryId != null || filterCityId != null
                || filterMinPrice != null || filterMaxPrice != null
                || (filterSortBy != null && !"newest".equals(filterSortBy));
    }

    private void runFilteredSearch() {
        if (!hasActiveFilterOrSearch()) { fetchAdsFromBackend(); return; }

        final String keyword = searchField != null ? searchField.getText() : null;
        new Thread(() -> {
            try {
                List<Item> items = ItemService.searchItems(
                        keyword, filterCategoryId, filterCityId, filterMinPrice, filterMaxPrice, filterSortBy);
                Platform.runLater(() -> renderItems(items));
            } catch (Exception e) {
                showLoadError(e);
            }
        }).start();
    }

    private void renderItems(List<Item> items) {
        if (loadingContainer != null) loadingContainer.setVisible(false);
        if (adsFlowPane == null) return;

        adsFlowPane.getChildren().clear();

        if (items == null || items.isEmpty()) {
            Label emptyLabel = new Label("هیچ آگهی‌ای برای نمایش وجود ندارد");
            emptyLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #94a3b8;");
            StackPane emptyPane = new StackPane(emptyLabel);
            emptyPane.setAlignment(Pos.CENTER);
            emptyPane.prefWidthProperty().bind(adsFlowPane.widthProperty());
            emptyPane.setPrefHeight(420);
            adsFlowPane.getChildren().add(emptyPane);
            return;
        }

        for (Item item : items) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(Routes.ITEM_AD));
                Parent card = loader.load();
                AdItemController controller = loader.getController();
                controller.setItem(item);
                card.setOnMouseClicked(event -> goToItemDetail(item));
                adsFlowPane.getChildren().add(card);
            } catch (Exception e) {
                System.err.println("❌ خطا در رندر کارت آگهی:");
                e.printStackTrace();
            }
        }
    }

    private void showLoadError(Exception e) {
        e.printStackTrace();
        Platform.runLater(() -> {
            if (loadingContainer != null) loadingContainer.setVisible(false);
            if (adsFlowPane != null) {
                adsFlowPane.getChildren().clear();
                Label errorLabel = new Label("خطا در بارگذاری آگهی‌ها: " + e.getMessage());
                errorLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 14px;");
                adsFlowPane.getChildren().add(errorLabel);
            }
        });
    }

    private void goToItemDetail(Item item) {
        try { MainApplication.goToItemDetail(item); }
        catch (Exception e) { System.err.println("❌ خطا در رفتن به صفحه جزئیات: " + e.getMessage()); }
    }

    @FXML
    private void showFilterDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Routes.FILTER_DIALOG));
            Parent root = loader.load();
            FilterDialogController controller = loader.getController();
            controller.setListener(this);
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(adsFlowPane.getScene().getWindow());
            dialog.setTitle("فیلترهای جست‌وجو");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public void onFilterApplied(Long categoryId, Long cityId, Long minPrice, Long maxPrice, String sortBy) {
        this.filterCategoryId = categoryId;
        this.filterCityId     = cityId;
        this.filterMinPrice   = minPrice;
        this.filterMaxPrice   = maxPrice;
        this.filterSortBy     = (sortBy == null || sortBy.isBlank()) ? "newest" : sortBy;
        runFilteredSearch();
    }

    @Override
    public void onFilterCleared() {
        this.filterCategoryId = null;
        this.filterCityId     = null;
        this.filterMinPrice   = null;
        this.filterMaxPrice   = null;
        this.filterSortBy     = "newest";
        runFilteredSearch();
    }

    private void setupMenuActions() {
        if (userMenuButton == null || userMenuButton.getItems() == null) return;
        for (MenuItem item : userMenuButton.getItems()) {
            item.setOnAction(event -> {
                String itemText = item.getText();
                if (itemText == null) return;
                switch (itemText.trim()) {
                    case "➕ ثبت آگهی جدید", "ثبت آگهی جدید" -> goToCreateAd();
                    case "📝 آگهی‌های من"                      -> goToMyAds();
                    case "❤️ علاقه‌مندی‌ها"                    -> goToFavorites();
                    case "💬 گفت‌وگوها"                        -> goToChats();
                    case "🛒 خریدها"                           -> goToPurchases();
                    case "👤 پروفایل"                          -> goToProfile();
                    case "خروج", "🚪 خروج"                    -> handleLogout();
                }
            });
        }
    }

    @FXML private void goToMyAds()      { try { MainApplication.changeScene(Routes.MY_ADS,      "آگهی‌های من");     } catch (Exception e) { e.printStackTrace(); } }
    @FXML private void goToFavorites()  { try { MainApplication.changeScene(Routes.FAVORITES,   "علاقه‌مندی‌ها");  } catch (Exception e) { e.printStackTrace(); } }
    @FXML private void goToChats()      { try { MainApplication.changeScene(Routes.CHATS,        "پیام‌ها");         } catch (Exception e) { e.printStackTrace(); } }
    @FXML private void goToPurchases()  { try { MainApplication.changeScene(Routes.PURCHASES,   "خریدها");          } catch (Exception e) { e.printStackTrace(); } }
    @FXML private void goToCreateAd()   { try { MainApplication.changeScene(Routes.CREATE_AD,   "ثبت آگهی جدید");  } catch (Exception e) { e.printStackTrace(); } }
    @FXML private void goToAdminPanel() { try { MainApplication.changeScene(Routes.ADMIN_PANEL, "پنل مدیریت");      } catch (Exception e) { e.printStackTrace(); } }
    @FXML private void goToProfile()    { try { MainApplication.changeScene(Routes.PROFILE,     "پروفایل من");      } catch (Exception e) { e.printStackTrace(); } }

    private void handleLogout() {
        SessionManager.logout();
        try { MainApplication.changeScene(Routes.LOGIN, "ورود"); }
        catch (Exception e) { e.printStackTrace(); }
    }
}