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
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold");

        TextField usernameField = new TextField();
        usernameField.setPromptText("نام کاربری");
        usernameField.setMaxWidth(300);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("رمز عبور");
        passwordField.setMaxWidth(300);

        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red;");

        Button loginButton = new Button("ورود");
        loginButton.setMaxWidth(300);

        Button goToRegister = new Button("حساب ندارید؟ ثبت نام کنید");
        goToRegister.setStyle("-fx-background-color: transparent; -fx-text-fill: blue;");

        loginButton.setOnAction(e -> {String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        if (username.isEmpty() || password.isEmpty()) {
        errorLabel.setText(("لطفاً همه فیلدها را پر کنید"));
        return;}
        //بعدا به بک وصلش میکنیم!
            System.out.println("Login: " + username);
        });

        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        layout.getChildren().addAll(title, usernameField, passwordField, errorLabel, loginButton, goToRegister);

        return new Scene(layout, 500, 400);
    }
}
