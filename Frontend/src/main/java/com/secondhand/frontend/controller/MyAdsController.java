package com.secondhand.frontend.controller;

import com.secondhand.frontend.util.FrontendErrorHandler;

import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.model.Item;
import com.secondhand.frontend.service.ItemService;
import com.secondhand.frontend.util.Routes;
import com.secondhand.frontend.util.WindowUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.util.List;

/**
 * JavaFX controller of the "my ads" screen.
 * <p>
 * This class is the JavaFX controller bound to its FXML file; it receives UI elements through the {@code @FXML} annotation, handles user events and talks to the backend through the service layer. Network calls run on a background thread and their results are applied on the UI thread via {@code Platform.runLater}.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class MyAdsController extends BaseController {

    @FXML private FlowPane myAdsFlowPane;
    @FXML private HBox     titleBar;

    /**
     * Initializes the controller after the FXML is loaded; wires event handlers and loads the initial data of the screen.
     */
    @FXML
    public void initialize() {
        WindowUtil.makeDraggable(titleBar);
        loadMyAds();
    }

    /**
     * Loads my ads.
     */
    private void loadMyAds() {
        ItemService.getMyItemsAsync()
                .thenAccept(myItems -> Platform.runLater(() -> renderItems(myItems)))
                .exceptionally(ex -> {
                    Platform.runLater(() -> showEmptyMessage("خطا در دریافت آگهی‌های من: " + ex.getMessage()));
                    return null;
                });
    }

    /**
     * Performs the "render items" operation.
     *
     * @param items the "items" value of type {@code List<Item>}
     */
    private void renderItems(List<Item> items) {
        myAdsFlowPane.getChildren().clear();
        if (items == null || items.isEmpty()) { showEmptyMessage("هنوز هیچ آگهی‌ای ثبت نکرده‌اید"); return; }
        for (Item item : items) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(Routes.ITEM_AD));
                Parent card = loader.load();
                AdItemController controller = loader.getController();
                controller.setItem(item);
                card.setOnMouseClicked(event -> goToItemDetail(item));
                myAdsFlowPane.getChildren().add(card);
            } catch (Exception e) { FrontendErrorHandler.log(e); }
        }
    }

    /**
     * Shows empty message.
     *
     * @param text the text value
     */
    private void showEmptyMessage(String text) {
        myAdsFlowPane.getChildren().clear();
        Label emptyLabel = new Label(text);
        emptyLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #94a3b8;");
        StackPane emptyPane = new StackPane(emptyLabel);
        emptyPane.setAlignment(Pos.CENTER);
        emptyPane.prefWidthProperty().bind(myAdsFlowPane.widthProperty());
        emptyPane.setPrefHeight(400);
        myAdsFlowPane.getChildren().add(emptyPane);
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