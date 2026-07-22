package com.secondhand.frontend.controller;

import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.model.ChatMessage;
import com.secondhand.frontend.model.Conversation;
import com.secondhand.frontend.service.ChatService;
import com.secondhand.frontend.util.Routes;
import com.secondhand.frontend.util.SessionManager;
import com.secondhand.frontend.util.WindowUtil;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.List;
import java.util.Optional;

public class ChatsController extends BaseController {

    @FXML private ListView<Conversation> chatUsersListView;
    @FXML private Label       currentChatUserLabel;
    @FXML private TextField   messageField;
    @FXML private VBox        messagesVBox;
    @FXML private ScrollPane  messagesScrollPane;
    @FXML private HBox        titleBar;

    private Conversation currentConversation;
    private static Long  initialConversationId;
    private Timeline     refreshTimeline;
    private int          lastMessageCount = 0;
    private static final int POLL_SECONDS = 3;

    public static void setInitialConversationId(Long id) { initialConversationId = id; }

    @FXML
    public void initialize() {
        WindowUtil.makeDraggable(titleBar);
        setupConversationList();
        loadConversations();
        startPolling();
    }

    private void setupConversationList() {
        Long myId = SessionManager.getCurrentUserId();
        chatUsersListView.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Conversation c, boolean empty) {
                super.updateItem(c, empty);
                setText(null);
                if (empty || c == null) { setGraphic(null); setStyle("-fx-background-color: transparent;"); return; }

                String other = c.getOtherPartyUsername(myId);
                Label itemLabel = new Label("\ud83d\udce6 " + c.getItemTitle());
                itemLabel.setStyle("-fx-text-fill: #0f172a; -fx-font-size: 13px; -fx-font-weight: bold;");
                Label userLabel = new Label("\ud83d\udc64 " + (other != null ? other : "کاربر"));
                userLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
                VBox texts = new VBox(2, itemLabel, userLabel);
                HBox.setHgrow(texts, Priority.ALWAYS);

                HBox row = new HBox(8, texts);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(8, 6, 8, 6));

                // دایرهٔ نارنجی پیام‌های خوانده‌نشده (مثل نوتیف)
                int unread = c.getUnreadCount() != null ? c.getUnreadCount() : 0;
                boolean isOpen = currentConversation != null && currentConversation.getId() != null
                        && currentConversation.getId().equals(c.getId());
                if (unread > 0 && !isOpen) {
                    Circle circle = new Circle(11);
                    circle.setStyle("-fx-fill: #f97316; -fx-effect: dropshadow(gaussian, rgba(249,115,22,0.45), 6, 0, 0, 1);");
                    Label count = new Label(unread > 99 ? "99+" : String.valueOf(unread));
                    count.setStyle("-fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold;");
                    StackPane badge = new StackPane(circle, count);
                    row.getChildren().add(badge);
                }

                setGraphic(row);
                setStyle("-fx-background-color: transparent;");
            }
        });
        chatUsersListView.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, newVal) -> {
                    if (newVal == null) return;
                    // فقط وقتی گفت‌وگوی دیگری انتخاب شد بازش کن (نه هنگام ریفرش لیست)
                    if (currentConversation == null || currentConversation.getId() == null
                            || !currentConversation.getId().equals(newVal.getId())) {
                        openConversation(newVal);
                    } else {
                        currentConversation = newVal;
                    }
                });
    }

    private void loadConversations() {
        new Thread(() -> {
            try {
                List<Conversation> conversations = ChatService.getConversations();
                Platform.runLater(() -> {
                    Conversation selected = chatUsersListView.getSelectionModel().getSelectedItem();
                    chatUsersListView.getItems().setAll(conversations);

                    if (initialConversationId != null) {
                        for (Conversation c : conversations) {
                            if (initialConversationId.equals(c.getId())) {
                                chatUsersListView.getSelectionModel().select(c);
                                chatUsersListView.scrollTo(c);
                                break;
                            }
                        }
                        initialConversationId = null;
                    } else if (selected != null) {
                        for (Conversation c : conversations) {
                            if (c.getId().equals(selected.getId())) {
                                chatUsersListView.getSelectionModel().select(c);
                                break;
                            }
                        }
                    } else if (conversations.isEmpty()) {
                        currentChatUserLabel.setText("هنوز هیچ گفت‌وگویی ندارید");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> currentChatUserLabel.setText("خطا در دریافت گفت‌وگوها"));
            }
        }).start();
    }

    private void openConversation(Conversation conversation) {
        currentConversation = conversation;
        Long myId = SessionManager.getCurrentUserId();
        String other = conversation.getOtherPartyUsername(myId);
        currentChatUserLabel.setText("\ud83d\udcac " + conversation.getItemTitle()
                + " — " + (other != null ? other : "کاربر"));
        lastMessageCount = 0;
        loadMessages(true);
        // باز کردن گفت‌وگو = خوانده شدن پیام‌ها ← حذف فوری دایرهٔ نارنجی
        conversation.setUnreadCount(0);
        chatUsersListView.refresh();
    }

    private void loadMessages(boolean scrollToBottom) {
        if (currentConversation == null) return;
        final Long conversationId = currentConversation.getId();
        new Thread(() -> {
            try {
                List<ChatMessage> messages = ChatService.getMessages(conversationId);
                Platform.runLater(() -> {
                    boolean hasNew = messages.size() > lastMessageCount;
                    renderMessages(messages);
                    if (scrollToBottom || hasNew) scrollToBottom();
                    lastMessageCount = messages.size();
                });
            } catch (Exception e) {
                System.err.println("[Chat Poll] خطا در دریافت پیام‌ها: " + e.getMessage());
            }
        }).start();
    }

    private void renderMessages(List<ChatMessage> messages) {
        messagesVBox.getChildren().clear();
        Long myId = SessionManager.getCurrentUserId();
        for (ChatMessage message : messages) {
            boolean mine = myId != null && myId.equals(message.getSenderId());
            Label bubble = new Label(message.getText());
            bubble.setWrapText(true);
            bubble.setMaxWidth(380);
            bubble.setStyle(mine
                    ? "-fx-background-color: #f97316; -fx-text-fill: white; -fx-background-radius: 14 14 2 14; -fx-padding: 9 13; -fx-effect: dropshadow(gaussian, rgba(249,115,22,0.25), 6, 0, 0, 2);"
                    : "-fx-background-color: #ffffff; -fx-text-fill: #0f172a; -fx-background-radius: 14 14 14 2; -fx-border-color: #e7ecf2; -fx-border-radius: 14 14 14 2; -fx-padding: 9 13;");

            // FIX (مورد ۴): نشانگر «ویرایش شده» زیر حباب پیام‌های ویرایش‌شده
            VBox bubbleBox = new VBox(2, bubble);
            bubbleBox.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
            if (message.isEdited()) {
                Label editedTag = new Label("ویرایش شده");
                editedTag.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 9px; -fx-font-style: italic; -fx-padding: 0 4;");
                bubbleBox.getChildren().add(editedTag);
            }

            HBox row;
            if (mine) {
                Button editBtn = new Button("✏");
                editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 0 2;");
                Tooltip.install(editBtn, new Tooltip("ویرایش پیام"));
                editBtn.setOnAction(e -> startEditMessage(message));

                Button deleteBtn = new Button("🗑");
                deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 0 2;");
                Tooltip.install(deleteBtn, new Tooltip("حذف پیام"));
                deleteBtn.setOnAction(e -> deleteMessage(message));

                VBox actions = new VBox(2, editBtn, deleteBtn);
                actions.setAlignment(Pos.CENTER);
                row = new HBox(6, actions, bubbleBox);
            } else {
                row = new HBox(bubbleBox);
            }
            row.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
            messagesVBox.getChildren().add(row);
        }
    }

    // FIX (مورد ۴): بعد از ویرایش، کل لیست پیام‌ها دوباره از سرور خوانده می‌شود تا نشانگر «ویرایش شده» هم نمایش داده شود
    private void startEditMessage(ChatMessage message) {
        TextInputDialog dialog = new TextInputDialog(message.getText());
        dialog.setTitle("ویرایش پیام");
        dialog.setHeaderText(null);
        dialog.setContentText("متن جدید:");
        try { dialog.getDialogPane().getStylesheets().add(getClass().getResource(Routes.STYLESHEET).toExternalForm()); } catch (Exception ignored) {}
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return;
        String newText = result.get().trim();
        if (newText.isEmpty()) return;
        final Long conversationId = currentConversation != null ? currentConversation.getId() : null;
        new Thread(() -> {
            try {
                ChatService.editMessage(message.getId(), newText);
                if (conversationId != null) {
                    List<ChatMessage> messages = ChatService.getMessages(conversationId);
                    Platform.runLater(() -> { renderMessages(messages); lastMessageCount = messages.size(); });
                }
            } catch (Exception e) {
                Platform.runLater(() -> currentChatUserLabel.setText("خطا در ویرایش پیام: " + e.getMessage()));
            }
        }).start();
    }

    // FIX: دیالوگ حذف پیام - از متد API بک‌اند موجود (DELETE /chat/message/{id}) که قبلاً بدون UI بود
    private void deleteMessage(ChatMessage message) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("حذف پیام");
        confirm.setHeaderText(null);
        confirm.setContentText("از حذف این پیام اطمینان دارید؟");
        try { confirm.getDialogPane().getStylesheets().add(getClass().getResource(Routes.STYLESHEET).toExternalForm()); } catch (Exception ignored) {}
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;
        final Long conversationId = currentConversation != null ? currentConversation.getId() : null;
        new Thread(() -> {
            try {
                ChatService.deleteMessage(message.getId());
                if (conversationId != null) {
                    List<ChatMessage> messages = ChatService.getMessages(conversationId);
                    Platform.runLater(() -> { renderMessages(messages); lastMessageCount = messages.size(); });
                }
            } catch (Exception e) {
                Platform.runLater(() -> currentChatUserLabel.setText("خطا در حذف پیام: " + e.getMessage()));
            }
        }).start();
    }

    private void scrollToBottom() {
        if (messagesScrollPane != null) { messagesScrollPane.layout(); messagesScrollPane.setVvalue(1.0); }
    }

    @FXML
    private void sendMessage() {
        if (currentConversation == null) { currentChatUserLabel.setText("ابتدا یک گفت‌وگو را انتخاب کنید"); return; }
        String text = messageField.getText() != null ? messageField.getText().trim() : "";
        if (text.isEmpty()) return;

        messageField.clear();
        final Long conversationId = currentConversation.getId();
        new Thread(() -> {
            try {
                ChatService.sendMessage(conversationId, text);
                List<ChatMessage> messages = ChatService.getMessages(conversationId);
                Platform.runLater(() -> { renderMessages(messages); scrollToBottom(); lastMessageCount = messages.size(); });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Label err = new Label("خطا در ارسال پیام: " + e.getMessage());
                    err.setStyle("-fx-text-fill: #dc2626;");
                    messagesVBox.getChildren().add(err);
                });
            }
        }).start();
    }

    @FXML
    private void goBack() {
        stopPolling();
        try { MainApplication.changeScene(Routes.AD_LIST, "لیست آگهی‌ها"); }
        catch (Exception e) { e.printStackTrace(); }
    }

    private void startPolling() {
        refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(POLL_SECONDS), event -> {
            if (currentConversation != null) loadMessages(false);
            loadConversations(); // بروزرسانی دایرهٔ نارنجی پیام‌های جدید
        }));
        refreshTimeline.setCycleCount(Animation.INDEFINITE);
        refreshTimeline.play();
    }

    private void stopPolling() {
        if (refreshTimeline != null) { refreshTimeline.stop(); refreshTimeline = null; }
    }

    @Override @FXML
    public void closeWindow(ActionEvent event) {
        stopPolling();
        super.closeWindow(event);
    }
}