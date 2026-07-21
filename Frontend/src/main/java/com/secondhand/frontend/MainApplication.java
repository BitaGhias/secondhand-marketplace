package com.secondhand.frontend;

import com.secondhand.frontend.controller.ItemDetailController;
import com.secondhand.frontend.model.Item;
import com.secondhand.frontend.util.BrandLogo;
import com.secondhand.frontend.util.Routes;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class MainApplication extends Application {

    private static Stage primaryStage;
    private static final int DEFAULT_WIDTH  = 1000;
    private static final int DEFAULT_HEIGHT = 1000;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        showSplash();
    }

    private void showSplash() {
        Stage splash = new Stage(StageStyle.TRANSPARENT);
        splash.setAlwaysOnTop(true);

        Label title  = new Label("بازار سفید");
        title.setStyle("-fx-font-size: 34px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");

        Label slogan = new Label("دستِ دوم، حالِ اول ✨");
        slogan.setStyle("-fx-font-size: 15px; -fx-text-fill: #0e9f6e; -fx-font-weight: bold;");

        VBox card = new VBox(16, BrandLogo.create(88), title, slogan);
        card.setAlignment(Pos.CENTER);
        card.setStyle(
                "-fx-background-color: #ffffff;" +
                        "-fx-background-radius: 26;" +
                        "-fx-border-color: #e3e8f0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 26;" +
                        "-fx-padding: 44 70;"
        );
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(15, 23, 42, 0.20));
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

        card.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(450), card);
        fadeIn.setFromValue(0); fadeIn.setToValue(1);

        PauseTransition hold = new PauseTransition(Duration.millis(1900));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), card);
        fadeOut.setFromValue(1); fadeOut.setToValue(0);

        SequentialTransition sequence = new SequentialTransition(fadeIn, hold, fadeOut);
        sequence.setOnFinished(e -> {
            splash.close();
            try { showLogin(); } catch (Exception ex) { ex.printStackTrace(); }
        });
        sequence.play();
    }

    private void showLogin() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(Routes.LOGIN));
        Parent root = loader.load();

        Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(getClass().getResource(Routes.STYLESHEET).toExternalForm());

        primaryStage.setTitle("بازار سفید - ورود");
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