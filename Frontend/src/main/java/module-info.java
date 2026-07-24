/**
 * Module descriptor of the Dibache JavaFX frontend; declares the required
 * JavaFX, HTTP-client and Jackson modules and opens the packages needed
 * for FXML injection and JSON deserialization.
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
module com.secondhand.frontend {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.net.http;
    requires java.logging;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.annotation;

    opens com.secondhand.frontend to javafx.fxml;
    exports com.secondhand.frontend;

    exports com.secondhand.frontend.controller;
    opens com.secondhand.frontend.controller to javafx.fxml;

    // اجازه دادن به جکسون و JavaFX برای دسترسی بازتابی به مدل‌ها
    exports com.secondhand.frontend.model;
    opens com.secondhand.frontend.model to com.fasterxml.jackson.databind, javafx.base;

    // اجازه دادن به جکسون برای سریالایز کردن کلاس‌های Request داخل سرویس‌ها
    exports com.secondhand.frontend.service;
    opens com.secondhand.frontend.service to com.fasterxml.jackson.databind;
    exports com.secondhand.frontend.util;
    opens com.secondhand.frontend.util to com.fasterxml.jackson.databind;
}
