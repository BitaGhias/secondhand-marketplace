package com.secondhand.frontend.controller;

import com.secondhand.frontend.util.FrontendErrorHandler;

import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.model.Item;
import com.secondhand.frontend.util.NotificationCenter;
import com.secondhand.frontend.util.Routes;
import com.secondhand.frontend.util.WindowUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * صفحه اعلان‌ها:
 *  • وضعیت آگهی‌های من (تایید/رد/در انتظار/فروخته‌شده)
 *  • درخواست‌های خرید رسیده برای آگهی‌های من
 *  • پاسخ فروشنده به درخواست‌های خرید من
 *  + دکمهٔ «خواندن» برای هر اعلان و «خواندن همه»
 */
public class NotificationsController extends BaseController {

    @FXML private VBox notificationsVBox;
    @FXML private Label headerCountLabel;
    @FXML private HBox titleBar;

    private List<NotificationCenter.Entry> entries = new ArrayList<>();

    @FXML
    public void initialize() {
        WindowUtil.makeDraggable(titleBar);
        refresh();
    }

    private void refresh() {
        new Thread(() -> {
            List<NotificationCenter.Entry> list = NotificationCenter.fetchAll();
            Set<String> read = NotificationCenter.readKeys();
            Platform.runLater(() -> render(list, read));
        }).start();
    }

    private void render(List<NotificationCenter.Entry> list, Set<String> read) {
        entries = list;
        notificationsVBox.getChildren().clear();

        if (list.isEmpty()) {
            headerCountLabel.setText("۰ اعلان");
            Label empty = new Label("🔕 فعلاً اعلانی ندارید");
            empty.setStyle("-fx-font-size: 15px; -fx-text-fill: #94a3b8; -fx-padding: 40;");
            notificationsVBox.getChildren().add(empty);
            return;
        }

        long unread = list.stream().filter(e -> !read.contains(e.key)).count();
        headerCountLabel.setText(unread > 0 ? unread + " خوانده‌نشده" : "همه خوانده شده");

        List<NotificationCenter.Entry> sorted = new ArrayList<>(list);
        sorted.sort(Comparator.comparing(e -> read.contains(e.key))); // خوانده‌نشده‌ها اول
        for (NotificationCenter.Entry e : sorted)
            notificationsVBox.getChildren().add(buildCard(e, read.contains(e.key)));
    }

    private Node buildCard(NotificationCenter.Entry e, boolean isRead) {
        Label icon = new Label(e.icon);
        icon.setStyle("-fx-font-size: 19px;");

        Label title = new Label(e.title != null ? e.title : "");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Label chip = new Label(e.chipText);
        chip.setStyle("-fx-background-color: " + e.chipBg + "; -fx-text-fill: " + e.chipFg
                + "; -fx-background-radius: 999; -fx-padding: 2 11; -fx-font-size: 10px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topRow = new HBox(9, icon, title, chip, spacer);
        topRow.setAlignment(Pos.CENTER_LEFT);
        if (!isRead) {
            Label newBadge = new Label("جدید");
            newBadge.setStyle("-fx-background-color: #f97316; -fx-text-fill: white; -fx-background-radius: 999; -fx-padding: 2 10; -fx-font-size: 10px; -fx-font-weight: bold;");
            topRow.getChildren().add(newBadge);
        }

        Label msg = new Label(e.message);
        msg.setWrapText(true);
        msg.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569;");

        VBox body = new VBox(6, topRow, msg);
        HBox.setHgrow(body, Priority.ALWAYS);

        if (e.rejectionReason != null && !e.rejectionReason.isBlank()) {
            Label reason = new Label("علت رد: " + e.rejectionReason);
            reason.setWrapText(true);
            reason.setStyle("-fx-background-color: #fef2f2; -fx-text-fill: #b91c1c; -fx-background-radius: 8; -fx-padding: 7 11; -fx-font-size: 11px;");
            body.getChildren().add(reason);
        }

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_LEFT);
        if (!isRead) {
            Button readBtn = new Button("✓ خواندن");
            readBtn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #16a34a; -fx-border-color: #bbf7d0; -fx-border-radius: 9; -fx-background-radius: 9; -fx-padding: 5 14; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand;");
            readBtn.setOnAction(ev -> { NotificationCenter.markRead(e.key); refresh(); });
            actions.getChildren().add(readBtn);
        }
        if (e.item != null && "REJECTED".equalsIgnoreCase(e.item.getStatus())) {
            Button editBtn = new Button("✏ ویرایش و ارسال مجدد");
            editBtn.setStyle("-fx-background-color: #f97316; -fx-text-fill: white; -fx-background-radius: 9; -fx-padding: 5 14; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand;");
            editBtn.setOnAction(ev -> goToEdit(e.item));
            actions.getChildren().add(editBtn);
        }
        if (e.openableItem && e.itemId != null) {
            Button viewBtn = new Button("📦 مشاهده آگهی");
            viewBtn.setStyle("-fx-background-color: #143449; -fx-text-fill: white; -fx-background-radius: 9; -fx-padding: 5 14; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand;");
            viewBtn.setOnAction(ev -> openItem(e.itemId));
            actions.getChildren().add(viewBtn);
        }
        if (!actions.getChildren().isEmpty()) body.getChildren().add(actions);

        Region edge = new Region();
        edge.setMinWidth(5); edge.setMaxWidth(5);
        edge.setStyle("-fx-background-color: " + e.edgeColor + "; -fx-background-radius: 999;");

        HBox card = new HBox(13, edge, body);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 14; -fx-border-color: #e7ecf2; -fx-border-radius: 14; -fx-padding: 13 15;"
                + (isRead ? " -fx-opacity: 0.62;" : " -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.06), 10, 0, 0, 3);"));
        return card;
    }

    @FXML
    private void markAllRead() {
        NotificationCenter.markAllRead(entries);
        refresh();
    }

    private void openItem(Long itemId) {
        try {
            ItemDetailController.setItemId(itemId);
            MainApplication.changeScene(Routes.ITEM_DETAIL, "جزئیات آگهی");
        } catch (Exception ex) { FrontendErrorHandler.log(ex); }
    }

    private void goToEdit(Item item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Routes.CREATE_AD));
            Parent root = loader.load();
            CreateAdController controller = loader.getController();
            controller.setItemForEdit(item);
            Stage stage = (Stage) notificationsVBox.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 1000);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            scene.getStylesheets().add(getClass().getResource(Routes.STYLESHEET).toExternalForm());
            stage.setScene(scene);
            stage.setTitle("ویرایش آگهی");
        } catch (Exception ex) { FrontendErrorHandler.log(ex); }
    }

    @FXML
    private void goBack() {
        try { MainApplication.changeScene(Routes.AD_LIST, "لیست آگهی‌ها"); }
        catch (Exception e) { FrontendErrorHandler.log(e); }
    }
}
