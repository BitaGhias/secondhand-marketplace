package com.secondhand.frontend;

import com.secondhand.frontend.util.FrontendErrorHandler;

import com.secondhand.frontend.controller.ItemDetailController;
import com.secondhand.frontend.model.Item;
import com.secondhand.frontend.model.User;
import com.secondhand.frontend.service.AuthService;
import com.secondhand.frontend.util.ApiClient;
import com.secondhand.frontend.util.BrandLogo;
import com.secondhand.frontend.util.Routes;
import com.secondhand.frontend.util.SessionManager;
import com.secondhand.frontend.util.SessionStore;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class MainApplication extends Application {

    private static Stage primaryStage;
    private static final int DEFAULT_WIDTH  = 1000;
    private static final int DEFAULT_HEIGHT = 1000;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        loadBundledFonts();
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        showSplash();
    }

    /** بارگذاری فونت فارسی همراه برنامه (Noto Sans Arabic) */
    private static void loadBundledFonts() {
        try {
            javafx.scene.text.Font.loadFont(MainApplication.class.getResourceAsStream("fonts/Vazirmatn-Regular.ttf"), 13);
            javafx.scene.text.Font.loadFont(MainApplication.class.getResourceAsStream("fonts/Vazirmatn-Bold.ttf"), 13);
        } catch (Exception ignored) { FrontendErrorHandler.log(ignored); }
    }

    private void showSplash() {
        Stage splash = new Stage(StageStyle.TRANSPARENT);
        splash.setAlwaysOnTop(true);

        // برند دو رنگ: «دست‌دوم» سفید + «مارکت» نارنجی
        Label titlePart1 = new Label("دست‌دوم ");
        titlePart1.setStyle("-fx-font-size: 34px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        Label titlePart2 = new Label("مارکت");
        titlePart2.setStyle("-fx-font-size: 34px; -fx-font-weight: bold; -fx-text-fill: #f97316;");
        HBox title = new HBox(titlePart2, titlePart1); // RTL: مارکت بعد از دست‌دوم نمایش داده می‌شود
        title.setAlignment(Pos.CENTER);
        title.setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);

        Label slogan = new Label("بخر، بفروش، پس‌انداز کن");
        slogan.setStyle("-fx-font-size: 15px; -fx-text-fill: #fdba74; -fx-font-weight: bold;");

        VBox card = new VBox(16, BrandLogo.create(88), title, slogan);
        card.setAlignment(Pos.CENTER);
        card.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #143449, #0e2433);" +
                        "-fx-background-radius: 26;" +
                        "-fx-border-color: rgba(255,255,255,0.10);" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 26;" +
                        "-fx-padding: 44 70;"
        );
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(14, 36, 51, 0.45));
        shadow.setRadius(34);
        shadow.setOffsetY(10);
        card.setEffect(shadow);

        StackPane root = new StackPane(card);
        root.setStyle("-fx-background-color: transparent;");
        root.setPadding(new Insets(40));

        Scene scene = new Scene(root, 480, 360);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(getClass().getResource(Routes.STYLESHEET).toExternalForm());
        splash.setScene(scene);
        splash.centerOnScreen();
        splash.show();

        // در حین نمایش اسپلش، نشست ذخیره‌شده (در صورت وجود) با سرور اعتبارسنجی می‌شود
        CompletableFuture<Boolean> autoLogin = tryRestoreSessionAsync();

        card.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(450), card);
        fadeIn.setFromValue(0); fadeIn.setToValue(1);

        PauseTransition hold = new PauseTransition(Duration.millis(1900));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), card);
        fadeOut.setFromValue(1); fadeOut.setToValue(0);

        SequentialTransition sequence = new SequentialTransition(fadeIn, hold, fadeOut);
        sequence.setOnFinished(e -> {
            splash.close();
            boolean restored = false;
            try {
                restored = autoLogin.get(1500, TimeUnit.MILLISECONDS);
            } catch (Exception ignored) { FrontendErrorHandler.log(ignored); }
            try {
                if (restored) {
                    showMainWindow(Routes.AD_LIST, "دست‌دوم مارکت — آگهی‌ها");
                } else {
                    showMainWindow(Routes.LOGIN, "دست‌دوم مارکت — ورود");
                }
            } catch (Exception ex) { FrontendErrorHandler.log(ex); }
        });
        sequence.play();
    }

    /**
     * تلاش برای بازیابی نشست قبلی: توکن ذخیره‌شده روی کلاینت ست می‌شود و
     * با دریافت پروفایل از سرور اعتبارسنجی می‌شود. اگر توکن نامعتبر باشد،
     * نشست پاک شده و کاربر به صفحه ورود هدایت می‌شود.
     */
    private CompletableFuture<Boolean> tryRestoreSessionAsync() {
        return CompletableFuture.supplyAsync(() -> {
            SessionStore.SavedSession saved = SessionStore.load();
            if (saved == null) return false;
            try {
                ApiClient.setToken(saved.token);
                User freshUser = AuthService.getProfile(); // اعتبارسنجی توکن با سرور
                SessionManager.setCurrentUser(freshUser != null ? freshUser : saved.user);
                return true;
            } catch (Exception e) {
                // توکن منقضی/نامعتبر یا سرور در دسترس نیست — نشست پاک می‌شود
                ApiClient.clearToken();
                SessionStore.clear();
                return false;
            }
        });
    }

    private void showMainWindow(String fxmlPath, String title) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(getClass().getResource(Routes.STYLESHEET).toExternalForm());

        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void changeScene(String fxmlPath, String title) throws Exception {
        FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource(fxmlPath));
        Parent root = loader.load();

        Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(MainApplication.class.getResource(Routes.STYLESHEET).toExternalForm());

        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void goToItemDetail(Item item) throws Exception {
        FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource(Routes.ITEM_DETAIL));
        Parent root = loader.load();
        ItemDetailController controller = loader.getController();
        controller.setItem(item);

        Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(MainApplication.class.getResource(Routes.STYLESHEET).toExternalForm());

        primaryStage.setTitle("جزئیات آگهی");
        primaryStage.setScene(scene);
    }

    public static void main(String[] args) { launch(args); }
}
