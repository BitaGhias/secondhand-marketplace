package com.secondhand.frontend.controller;

import com.secondhand.frontend.util.FrontendErrorHandler;

import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.model.Item;
import com.secondhand.frontend.service.FavoriteService;
import com.secondhand.frontend.util.Routes;
import com.secondhand.frontend.util.WindowUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;

import java.util.List;

/**
 * JavaFX controller of the "favorites" screen.
 * <p>
 * This class is the JavaFX controller bound to its FXML file; it receives UI elements through the {@code @FXML} annotation, handles user events and talks to the backend through the service layer. Network calls run on a background thread and their results are applied on the UI thread via {@code Platform.runLater}.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class FavoritesController extends BaseController {

    @FXML private FlowPane favoritesFlowPane;
    @FXML private Label    noFavoritesLabel;
    @FXML private HBox     titleBar;

    /**
     * Initializes the controller after the FXML is loaded; wires event handlers and loads the initial data of the screen.
     */
    @FXML
    public void initialize() {
        WindowUtil.makeDraggable(titleBar);
        loadFavorites();
    }

    /**
     * Loads favorites.
     */
    private void loadFavorites() {
        new Thread(() -> {
            try {
                List<Item> favorites = FavoriteService.getFavorites();
                Platform.runLater(() -> {
                    favoritesFlowPane.getChildren().clear();
                    if (favorites.isEmpty()) {
                        favoritesFlowPane.getChildren().add(noFavoritesLabel);
                        noFavoritesLabel.setVisible(true);
                    } else {
                        for (Item item : favorites) {
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource(Routes.ITEM_AD));
                                Parent card = loader.load();
                                AdItemController controller = loader.getController();
                                controller.setItem(item);
                                card.setOnMouseClicked(event -> goToItemDetail(item));
                                favoritesFlowPane.getChildren().add(card);
                            } catch (Exception e) { FrontendErrorHandler.log(e); }
                        }
                    }
                });
            } catch (Exception e) {
                FrontendErrorHandler.log(e);
                Platform.runLater(() -> {
                    noFavoritesLabel.setText("خطا در دریافت علاقه‌مندی‌ها: " + e.getMessage());
                    noFavoritesLabel.setVisible(true);
                });
            }
        }).start();
    }

    /**
     * Navigates to to item detail.
     *
     * @param item the ad (item) object
     */
    private void goToItemDetail(Item item) {
        try { MainApplication.goToItemDetail(item); }
        catch (Exception e) { System.err.println("❌ خطا در رفتن به جزئیات: " + e.getMessage()); }
    }

    /**
     * Navigates to back.
     */
    @FXML
    private void goBack() {
        try { MainApplication.changeScene(Routes.AD_LIST, "لیست آگهی‌ها"); }
        catch (Exception e) { FrontendErrorHandler.log(e); }
    }
}