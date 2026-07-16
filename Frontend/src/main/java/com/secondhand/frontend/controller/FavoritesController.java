package com.secondhand.frontend.controller;

import com.secondhand.frontend.model.Item;
import com.secondhand.frontend.service.FavoriteService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

import java.util.List;

public class FavoritesController {

    @FXML private FlowPane favoritesFlowPane;
    @FXML private Label noFavoritesLabel;

    @FXML
    public void initialize() {
        loadFavorites();
    }

    private void loadFavorites() {
        try {
            List<Item> favorites = FavoriteService.getFavorites();
            Platform.runLater(() -> {
                favoritesFlowPane.getChildren().clear();

                if (favorites.isEmpty()) {
                    noFavoritesLabel.setVisible(true);
                } else {
                    noFavoritesLabel.setVisible(false);
                    for (Item item : favorites) {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/secondhand/frontend/item_ad.fxml"));
                            Parent card = loader.load();
                            ItemAdController controller = loader.getController();
                            controller.setItem(item);
                            favoritesFlowPane.getChildren().add(card);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}