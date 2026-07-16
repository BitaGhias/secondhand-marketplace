module com.secondhand.frontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires javafx.swing;
    requires com.fasterxml.jackson.datatype.jsr310;

    opens com.secondhand.frontend to javafx.fxml;
    exports com.secondhand.frontend;

    exports com.secondhand.frontend.controller;
    opens com.secondhand.frontend.controller to javafx.fxml;

    // 🟢 اضافه شده: اجازه دادن به جکسون برای خواندن و ساختن اشیاء مدل Ad و User
    exports com.secondhand.frontend.model;
    opens com.secondhand.frontend.model to com.fasterxml.jackson.databind;
}