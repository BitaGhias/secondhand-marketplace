package com.secondhand.frontend.controller;

import com.secondhand.frontend.util.WindowUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;

public abstract class BaseController {

    @FXML
    public void minimizeWindow(ActionEvent event) {
        WindowUtil.minimize((Node) event.getSource());
    }

    @FXML
    public void maximizeWindow(ActionEvent event) {
        WindowUtil.toggleMaximize((Node) event.getSource());
    }

    @FXML
    public void closeWindow(ActionEvent event) {
        WindowUtil.close((Node) event.getSource());
    }
}