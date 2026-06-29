package com.secondhand.frontend;

import controller.LoginController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) {
        LoginController login = new LoginController();
        stage.setScene(login.getScene(stage));
        stage.setTitle("Second-hand Project");
        stage.show();
    }
    public static void main(String[] args) {
        launch();
    }
}
