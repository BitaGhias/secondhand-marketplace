package com.secondhand.frontend;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color; // ۱. این ایمپورت اضافه شد
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainApplication extends Application {

    private static Stage primaryStage;
    private static final int DEFAULT_WIDTH = 500;
    private static final int DEFAULT_HEIGHT = 1000;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage; // برای تغییر صحنه از جاهای مختلف برنامه

        // لود کردن صفحه ورود
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/secondhand/frontend/login.fxml"));
        Parent root = loader.load(); // شامل همه چیزهایی که تو صفحه نمایش داده میشه

        Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT); // اولی منظور محتوای توی صفحه

        // ۲. شفاف کردن بک‌گراند سین (Scene) تا دسکتاپ از پشت لبه‌های گرد دیده شود
        scene.fillProperty().set(Color.TRANSPARENT);

        scene.getStylesheets().add(getClass().getResource("/com/secondhand/frontend/css/styles.css").toExternalForm());

        stage.setTitle("فروشگاه دست دوم - ورود");
        stage.setScene(scene);
        stage.setResizable(false); // صفحات ورود و ثبت نام غیرقابل تغییر

        // ۳. تغییر استایل به TRANSPARENT برای حذف کامل فریم بیرونی
        stage.initStyle(StageStyle.TRANSPARENT);

        stage.show();
    }

    public static void changeScene(String fxmlPath, String title) throws Exception {
        FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource(fxmlPath));
        Parent root = loader.load();
        Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);

        // نکته: اگر صفحات بعدی (مثلاً پنل اصلی) هم قرار است بدون فریم و گرد باشند،
        // خط زیر را در متد changeScene هم قرار دهید:
        // scene.fillProperty().set(Color.TRANSPARENT);

        scene.getStylesheets().add(MainApplication.class.getResource("/com/secondhand/frontend/css/styles.css").toExternalForm());
        scene.fillProperty().set(Color.TRANSPARENT);
        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}