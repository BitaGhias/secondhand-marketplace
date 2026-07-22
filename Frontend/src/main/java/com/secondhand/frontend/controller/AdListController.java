package com.secondhand.frontend.controller;

import com.secondhand.frontend.util.FrontendErrorHandler;

import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.model.Category;
import com.secondhand.frontend.model.City;
import com.secondhand.frontend.model.Item;
import com.secondhand.frontend.model.User;
import com.secondhand.frontend.service.CategoryService;
import com.secondhand.frontend.service.CityService;
import com.secondhand.frontend.service.ItemService;
import com.secondhand.frontend.util.CategoryPicker;
import com.secondhand.frontend.util.NotificationCenter;
import com.secondhand.frontend.util.Routes;
import com.secondhand.frontend.util.SessionManager;
import com.secondhand.frontend.util.WindowUtil;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * صفحهٔ اصلی بازار:
 * • Hero با آمار زنده + بج اعلان روی زنگوله
 * • سایدبار فیلتر داخل خود صفحه: چک‌باکس دسته‌بندی + اسلایدر قیمت + شهر + مرتب‌سازی
 * • نتایج با انیمیشن fade-in پلکانی
 */
public class AdListController extends BaseController {

    @FXML private MenuButton userMenuButton;
    @FXML private MenuButton categoryQuickMenuButton;
    @FXML private TextField  searchField;
    @FXML private FlowPane   adsFlowPane;
    @FXML private VBox       loadingContainer;
    @FXML private HBox       titleBar;
    @FXML private Label      bellBadgeLabel;
    @FXML private Label      statAdsLabel;
    @FXML private Label      statCategoriesLabel;
    @FXML private Label      statCitiesLabel;

    // سایدبار فیلتر
    @FXML private VBox            categoryChecksVBox;
    @FXML private Slider          maxPriceSlider;
    @FXML private Label           priceValueLabel;
    @FXML private ComboBox<City>  cityFilterComboBox;
    @FXML private ComboBox<String> sortFilterComboBox;
    @FXML private Label           resultsCountLabel;

    private final PauseTransition searchDebounce = new PauseTransition(Duration.millis(400));

    private static final long PRICE_SLIDER_MAX = 100_000_000L;

    /** برچسب فارسی مرتب‌سازی ← کد مورد انتظار بک‌اند (ItemSearchRequest.sortBy) */
    private static final Map<String, String> SORT_OPTIONS = new LinkedHashMap<>();
    static {
        SORT_OPTIONS.put("جدیدترین", "newest");
        SORT_OPTIONS.put("قدیمی‌ترین", "oldest");
        SORT_OPTIONS.put("ارزان‌ترین", "price_asc");
        SORT_OPTIONS.put("گران‌ترین", "price_desc");
    }
    private static final String DEFAULT_SORT_LABEL = "جدیدترین";

    /** دستهٔ انتخاب‌شده از فلای‌اوت نوبار (سمت سرور اعمال می‌شود) */
    private Long quickCategoryId;

    private List<Category> allCategories = new ArrayList<>();
    private final List<CheckBox> categoryChecks = new ArrayList<>();

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

        setupFilterSidebar();
        loadCategoriesAndCities();
        fetchAdsFromBackend();
        loadNotificationBadge();

        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                searchDebounce.setOnFinished(e -> runFilteredSearch());
                searchDebounce.playFromStart();
            });
        }
    }

    // ===================== سایدبار فیلتر =====================

    private void setupFilterSidebar() {
        if (maxPriceSlider != null) {
            maxPriceSlider.setMin(0);
            maxPriceSlider.setMax(PRICE_SLIDER_MAX);
            maxPriceSlider.setValue(PRICE_SLIDER_MAX);
            maxPriceSlider.setBlockIncrement(1_000_000);
            maxPriceSlider.valueProperty().addListener((obs, o, v) -> updatePriceLabel(v.longValue()));
            updatePriceLabel(PRICE_SLIDER_MAX);
        }
        if (sortFilterComboBox != null) {
            sortFilterComboBox.getItems().setAll(SORT_OPTIONS.keySet());
            sortFilterComboBox.setValue(DEFAULT_SORT_LABEL);
        }
        if (cityFilterComboBox != null) {
            cityFilterComboBox.setConverter(new StringConverter<>() {
                @Override public String toString(City c) { return c == null ? "همهٔ شهرها" : c.getName(); }
                @Override public City fromString(String s) { return null; }
            });
        }
    }

    private void updatePriceLabel(long value) {
        if (priceValueLabel == null) return;
        priceValueLabel.setText(value >= PRICE_SLIDER_MAX
                ? "بدون محدودیت"
                : String.format("تا %,d تومان", value));
    }

    /** دسته‌ها (چک‌باکس‌های سایدبار + فلای‌اوت نوبار) و شهرها + آمار Hero */
    private void loadCategoriesAndCities() {
        new Thread(() -> {
            try {
                List<Category> categories = CategoryService.getAllCategories();
                Platform.runLater(() -> {
                    allCategories = categories;
                    buildCategoryChecks(categories);
                    if (categoryQuickMenuButton != null) {
                        CategoryPicker.populate(categoryQuickMenuButton, categories, "🗂 همه دسته‌ها", cat -> {
                            this.quickCategoryId = cat != null ? cat.getId() : null;
                            runFilteredSearch();
                        });
                    }
                    if (statCategoriesLabel != null) statCategoriesLabel.setText(String.valueOf(categories.size()));
                });
            } catch (Exception ignored) { }
            try {
                List<City> cities = CityService.getAllCities();
                Platform.runLater(() -> {
                    if (cityFilterComboBox != null) cityFilterComboBox.getItems().setAll(cities);
                    if (statCitiesLabel != null) statCitiesLabel.setText(String.valueOf(cities.size()));
                });
            } catch (Exception ignored) { }
        }).start();
    }

    /** چک‌باکس دسته‌بندی‌ها — دسته‌های اصلی پررنگ، زیردسته‌ها تورفتگی‌دار */
    private void buildCategoryChecks(List<Category> categories) {
        if (categoryChecksVBox == null) return;
        categoryChecksVBox.getChildren().clear();
        categoryChecks.clear();
        for (Category root : categories) {
            if (root.getParentId() == null) addCategoryCheck(root, 0);
        }
    }

    private void addCategoryCheck(Category category, int depth) {
        if (depth > 8) return;
        CheckBox check = new CheckBox(category.getName());
        check.setUserData(category.getId());
        check.setStyle("-fx-text-fill: " + (depth == 0 ? "#0f172a; -fx-font-weight: bold;" : "#475569;")
                + " -fx-font-size: 12px; -fx-padding: 2 " + (depth * 14) + " 2 0; -fx-cursor: hand;");
        check.selectedProperty().addListener((obs, o, v) -> runFilteredSearch());
        categoryChecksVBox.getChildren().add(check);
        categoryChecks.add(check);
        for (Category c : allCategories) {
            if (c.getParentId() != null && c.getParentId().equals(category.getId())) {
                addCategoryCheck(c, depth + 1);
            }
        }
    }

    /** مجموعهٔ دسته‌های تیک‌خورده + همهٔ زیردسته‌هایشان (برای فیلتر سمت کلاینت) */
    private Set<Long> selectedCategoryIds() {
        Set<Long> selected = new HashSet<>();
        for (CheckBox check : categoryChecks) {
            if (check.isSelected() && check.getUserData() instanceof Long id) selected.add(id);
        }
        if (selected.isEmpty()) return selected;
        boolean changed = true;
        int guard = 0;
        while (changed && guard++ < 16) {
            changed = false;
            for (Category c : allCategories) {
                if (c.getId() != null && !selected.contains(c.getId())
                        && c.getParentId() != null && selected.contains(c.getParentId())) {
                    selected.add(c.getId());
                    changed = true;
                }
            }
        }
        return selected;
    }

    @FXML private void applyFilters() { runFilteredSearch(); }

    @FXML
    private void clearFilters() {
        for (CheckBox check : categoryChecks) check.setSelected(false);
        if (maxPriceSlider != null) maxPriceSlider.setValue(PRICE_SLIDER_MAX);
        if (cityFilterComboBox != null) cityFilterComboBox.setValue(null);
        if (sortFilterComboBox != null) sortFilterComboBox.setValue(DEFAULT_SORT_LABEL);
        if (searchField != null) searchField.clear();
        quickCategoryId = null;
        if (categoryQuickMenuButton != null) categoryQuickMenuButton.setText("🗂 همه دسته‌ها");
        runFilteredSearch();
    }

    // ===================== دریافت و نمایش آگهی‌ها =====================

    private Long currentMaxPrice() {
        if (maxPriceSlider == null) return null;
        long v = (long) maxPriceSlider.getValue();
        return v >= PRICE_SLIDER_MAX ? null : v;
    }

    private Long currentCityId() {
        City city = cityFilterComboBox != null ? cityFilterComboBox.getValue() : null;
        return city != null ? city.getId() : null;
    }

    private String currentSortBy() {
        return sortFilterComboBox != null
                ? SORT_OPTIONS.getOrDefault(sortFilterComboBox.getValue(), "newest")
                : "newest";
    }

    private boolean hasActiveFilterOrSearch() {
        String keyword = searchField != null ? searchField.getText() : null;
        return (keyword != null && !keyword.isBlank())
                || quickCategoryId != null || currentCityId() != null || currentMaxPrice() != null
                || !selectedCategoryIds().isEmpty()
                || !"newest".equals(currentSortBy());
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

    private void runFilteredSearch() {
        if (!hasActiveFilterOrSearch()) { fetchAdsFromBackend(); return; }

        final String keyword  = searchField != null ? searchField.getText() : null;
        final Long cityId     = currentCityId();
        final Long maxPrice   = currentMaxPrice();
        final String sortBy   = currentSortBy();
        final Set<Long> catIds = selectedCategoryIds();

        new Thread(() -> {
            try {
                List<Item> items = ItemService.searchItems(keyword, quickCategoryId, cityId, null, maxPrice, sortBy);
                // فیلتر چنددسته‌ای چک‌باکس‌ها سمت کلاینت اعمال می‌شود
                // (بک‌اند در هر جست‌وجو فقط یک categoryId می‌پذیرد)
                final List<Item> visible;
                if (!catIds.isEmpty()) {
                    visible = items.stream()
                            .filter(i -> i.getCategoryId() != null && catIds.contains(i.getCategoryId()))
                            .toList();
                } else {
                    visible = items;
                }
                Platform.runLater(() -> renderItems(visible));
            } catch (Exception e) {
                showLoadError(e);
            }
        }).start();
    }

    private void renderItems(List<Item> items) {
        if (loadingContainer != null) loadingContainer.setVisible(false);
        if (adsFlowPane == null) return;

        adsFlowPane.getChildren().clear();

        int count = items != null ? items.size() : 0;
        if (statAdsLabel != null) statAdsLabel.setText(String.format("%,d", count));
        if (resultsCountLabel != null) resultsCountLabel.setText(count + " آگهی");

        if (items == null || items.isEmpty()) {
            Label emptyLabel = new Label("🔍 هیچ آگهی‌ای با این فیلترها پیدا نشد");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #94a3b8;");
            StackPane emptyPane = new StackPane(emptyLabel);
            emptyPane.setAlignment(Pos.CENTER);
            emptyPane.setPrefWidth(620);
            emptyPane.setPrefHeight(360);
            adsFlowPane.getChildren().add(emptyPane);
            return;
        }

        int index = 0;
        for (Item item : items) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(Routes.ITEM_AD));
                Parent card = loader.load();
                AdItemController controller = loader.getController();
                controller.setItem(item);
                card.setOnMouseClicked(event -> goToItemDetail(item));
                adsFlowPane.getChildren().add(card);

                // انیمیشن fade-in پلکانی برای نتایج
                card.setOpacity(0);
                FadeTransition fade = new FadeTransition(Duration.millis(300), card);
                fade.setFromValue(0);
                fade.setToValue(1);
                fade.setDelay(Duration.millis(Math.min(index, 14) * 45L));
                fade.play();
                index++;
            } catch (Exception e) {
                System.err.println("❌ خطا در رندر کارت آگهی:");
                FrontendErrorHandler.log(e);
            }
        }
    }

    private void showLoadError(Exception e) {
        FrontendErrorHandler.log(e);
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

    // ===================== اعلان‌ها =====================

    /** بج زنگوله: تعداد اعلان‌های خوانده‌نشده (وضعیت آگهی‌ها + درخواست‌های خرید) */
    private void loadNotificationBadge() {
        new Thread(() -> {
            try {
                long unread = NotificationCenter.unreadCount();
                Platform.runLater(() -> {
                    if (bellBadgeLabel != null) {
                        bellBadgeLabel.setText(unread > 99 ? "99+" : String.valueOf(unread));
                        bellBadgeLabel.setVisible(unread > 0);
                        bellBadgeLabel.setManaged(unread > 0);
                    }
                });
            } catch (Exception ignored) { }
        }).start();
    }

    @FXML
    private void goToNotifications() {
        try { MainApplication.changeScene(Routes.NOTIFICATIONS, "اعلان‌ها"); }
        catch (Exception e) { FrontendErrorHandler.log(e); }
    }

    // ===================== ناوبری =====================

    @FXML
    private void handleSearchClick() { runFilteredSearch(); }

    private void goToItemDetail(Item item) {
        try { MainApplication.goToItemDetail(item); }
        catch (Exception e) { System.err.println("❌ خطا در رفتن به صفحه جزئیات: " + e.getMessage()); }
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
                    case "🔔 اعلان‌ها"                          -> goToNotifications();
                    case "👤 پروفایل"                          -> goToProfile();
                    case "خروج", "🚪 خروج"                    -> handleLogout();
                }
            });
        }
    }

    @FXML private void goToMyAds()      { try { MainApplication.changeScene(Routes.MY_ADS,      "آگهی‌های من");     } catch (Exception e) { FrontendErrorHandler.log(e); } }
    @FXML private void goToFavorites()  { try { MainApplication.changeScene(Routes.FAVORITES,   "علاقه‌مندی‌ها");  } catch (Exception e) { FrontendErrorHandler.log(e); } }
    @FXML private void goToChats()      { try { MainApplication.changeScene(Routes.CHATS,        "پیام‌ها");         } catch (Exception e) { FrontendErrorHandler.log(e); } }
    @FXML private void goToPurchases()  { try { MainApplication.changeScene(Routes.PURCHASES,   "خریدها");          } catch (Exception e) { FrontendErrorHandler.log(e); } }
    @FXML private void goToCreateAd()   { try { MainApplication.changeScene(Routes.CREATE_AD,   "ثبت آگهی جدید");  } catch (Exception e) { FrontendErrorHandler.log(e); } }
    @FXML private void goToAdminPanel() { try { MainApplication.changeScene(Routes.ADMIN_PANEL, "پنل مدیریت");      } catch (Exception e) { FrontendErrorHandler.log(e); } }
    @FXML private void goToProfile()    { try { MainApplication.changeScene(Routes.PROFILE,     "پروفایل من");      } catch (Exception e) { FrontendErrorHandler.log(e); } }

    private void handleLogout() {
        SessionManager.logout();
        try { MainApplication.changeScene(Routes.LOGIN, "ورود"); }
        catch (Exception e) { FrontendErrorHandler.log(e); }
    }
}
