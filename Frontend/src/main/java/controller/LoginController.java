package controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginController {
    public Scene getScene(Stage stage) {
        Label title = new Label("ورود به سامانه");
        title.setStyle("-fx-font-family: 'B Yekan', 'Segoe UI', Tahoma; " +
                "-fx-font-size: 24px; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: #2c3e50;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("نام کاربری");
        usernameField.setMaxWidth(300);
        usernameField.setPrefHeight(40);
        usernameField.setStyle("-fx-background-radius: 8px; " +
                "-fx-border-radius: 8px; " +
                "-fx-border-color: #bdc3c7; " +
                "-fx-background-color: #ffffff; " +
                "-fx-alignment: center-right; " +
                "-fx-font-size: 14px;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("رمز عبور");
        passwordField.setMaxWidth(300);
        passwordField.setPrefHeight(40);
        passwordField.setStyle("-fx-background-radius: 8px; " +
                "-fx-border-radius: 8px; " +
                "-fx-border-color: #bdc3c7; " +
                "-fx-background-color: #ffffff; " +
                "-fx-alignment: center-right; " +
                "-fx-font-size: 14px;");

        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px; -fx-font-weight: bold;");

        Button loginButton = new Button("ورود");
        loginButton.setMaxWidth(300);
        loginButton.setPrefHeight(42);
        String baseButtonStyle = "-fx-background-color: #3498db; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 15px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 8px; " +
                "-fx-cursor: hand;";
        loginButton.setStyle(baseButtonStyle);

        loginButton.setOnMouseEntered(e -> loginButton.setStyle(baseButtonStyle + "-fx-background-color: #2980b9;"));
        loginButton.setOnMouseExited(e -> loginButton.setStyle(baseButtonStyle));

        Button goToRegister = new Button("حساب ندارید؟ ثبت نام کنید");
        goToRegister.setStyle("-fx-background-color: transparent; " +
                "-fx-text-fill: #7f8c8d; " +
                "-fx-font-size: 13px; " +
                "-fx-cursor: hand; " +
                "-fx-underline: true;");
        goToRegister.setOnMouseEntered(e -> goToRegister.setStyle("-fx-background-color: transparent; -fx-text-fill: #34495e; -fx-font-size: 13px; -fx-cursor: hand; -fx-underline: true;"));
        goToRegister.setOnMouseExited(e -> goToRegister.setStyle("-fx-background-color: transparent; -fx-text-fill: #7f8c8d; -fx-font-size: 13px; -fx-cursor: hand; -fx-underline: true;"));

        loginButton.setOnAction(e -> {String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        if (username.isEmpty() || password.isEmpty()) {
        errorLabel.setText(("لطفاً همه فیلدها را پر کنید"));
        return;}
        //بعدا به بک وصلش میکنیم!
            errorLabel.setText("");
            System.out.println("Login: " + username);
        });

        VBox layout = new VBox(18);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        layout.setStyle("-fx-background-color: #f5f6fa;");
        layout.getChildren().addAll(title, usernameField, passwordField, errorLabel, loginButton, goToRegister);

        return new Scene(layout, 500, 420);
    }
}
