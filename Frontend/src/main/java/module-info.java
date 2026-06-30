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
    requires com.almasb.fxgl.all;

    opens com.secondhand.frontend to javafx.fxml;
    exports com.secondhand.frontend;

    exports com.secondhand.frontend.controller;
    opens com.secondhand.frontend.controller to javafx.fxml;

    exports com.secondhand.frontend.config;
    opens com.secondhand.frontend.config to javafx.fxml;
}