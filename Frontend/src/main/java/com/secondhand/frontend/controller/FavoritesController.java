package com.secondhand.frontend.controller;

import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.model.Item;
import com.secondhand.frontend.service.FavoriteService;
import com.secondhand.frontend.util.WindowUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.List;

public class FavoritesController extends BaseController {
    @FXML private FlowPane favoritesFlowPane;
    @FXML private Label noFavoritesLabel;
    @FXML private HBox titleBar;

    @FXML
    public void initialize() {
        WindowUtil.makeDraggable(titleBar);
        loadFavorites();
    }

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
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/secondhand/frontend/item_ad.fxml"));
                                Parent card = loader.load();
                                ItemAdController controller = loader.getController();
                                controller.setItem(item);
                                card.setOnMouseClicked(event -> goToItemDetail(item));
                                favoritesFlowPane.getChildren().add(card);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    noFavoritesLabel.setText("خطا در دریافت علاقه‌مندی‌ها: " + e.getMessage());
                    noFavoritesLabel.setVisible(true);
                });
            }
        }).start();
    }

    private void goToItemDetail(Item item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/secondhand/frontend/item_detail.fxml"));
            Parent root = loader.load();
            ItemDetailController controller = loader.getController();
            controller.setItem(item);

            Stage stage = (Stage) favoritesFlowPane.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 1000);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            scene.getStylesheets().add(getClass().getResource("/com/secondhand/frontend/css/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("جزئیات آگهی");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goBack() {
        try {
            MainApplication.changeScene("/com/secondhand/frontend/adlist.fxml", "لیست آگهی‌ها");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
