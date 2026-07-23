package com.secondhand.frontend.controller;

import com.secondhand.frontend.util.FrontendErrorHandler;
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

                Label itemLabel = new Label("\ud83d\udce6 " + c.getItemTitle());
                itemLabel.setStyle("-fx-text-fill: #0f172a; -fx-font-size: 13px; -fx-font-weight: bold;");

                String parties = "👤 " + c.getBuyerUsername() + " ↔ " + c.getSellerUsername();
                Label userLabel = new Label(parties);
                userLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");

                Label lastMsgLabel = new Label();
                if (c.getLastMessage() != null && !c.getLastMessage().isBlank()) {
                    String preview = c.getLastMessage();
                    if (preview.length() > 30) preview = preview.substring(0, 30) + "...";
                    lastMsgLabel.setText("\ud83d\udcac " + preview);
                    lastMsgLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px; -fx-font-style: italic;");
                }

                Label timeLabel = new Label();
                if (c.getLastMessageTime() != null) {
                    timeLabel.setText(c.getLastMessageTime());
                    timeLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px;");
                }

                VBox texts = new VBox(2, itemLabel, userLabel);
                if (lastMsgLabel.getText() != null) texts.getChildren().add(lastMsgLabel);
                HBox.setHgrow(texts, Priority.ALWAYS);

                HBox row = new HBox(8, texts);
                if (timeLabel.getText() != null) row.getChildren().add(timeLabel);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(8, 6, 8, 6));

                int unread = c.getUnreadCount() != null ? c.getUnreadCount().intValue() : 0;
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

                if (c.isOtherPartyBlocked()) {
                    row.setOpacity(0.6);
                    row.setStyle("-fx-background-color: #fef2f2;");
                    Tooltip.install(row, new Tooltip("⚠️ شما یا طرف مقابل مسدود شده‌اید"));
                }

                setGraphic(row);
                setStyle("-fx-background-color: transparent;");
            }
        });

        chatUsersListView.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, newVal) -> {
                    if (newVal == null) return;
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

        String title = "\ud83d\udcac " + conversation.getItemTitle() + " — " + (other != null ? other : "کاربر");
        if (conversation.isOtherPartyBlocked()) {
            title += " ⚠️ (مسدود)";
            messageField.setDisable(true);
        } else {
            messageField.setDisable(false);
        }
        currentChatUserLabel.setText(title);
        lastMessageCount = 0;
        loadMessages(true);
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
            String text = message.isDeleted() ? "این پیام حذف شده است" : message.getText();

            Label bubble = new Label(text);
            bubble.setWrapText(true);
            bubble.setMaxWidth(380);

            VBox bubbleBox = new VBox(2);
            bubbleBox.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

            HBox msgRow = new HBox(4, bubble);
            msgRow.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

            Label timeLabel = new Label();
            if (message.getShortTime() != null) {
                timeLabel.setText(message.getShortTime());
                timeLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 9px;");
            }

            if (mine) {
                bubble.setStyle("-fx-background-color: #f97316; -fx-text-fill: white; -fx-background-radius: 14 14 2 14; -fx-padding: 9 13; -fx-effect: dropshadow(gaussian, rgba(249,115,22,0.25), 6, 0, 0, 2);");
                bubbleBox.getChildren().addAll(bubble, timeLabel);

                if (message.isEdited()) {
                    Label editedTag = new Label("✏️ ویرایش شده");
                    editedTag.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 9px; -fx-font-style: italic; -fx-padding: 0 4;");
                    bubbleBox.getChildren().add(editedTag);
                }

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

                HBox row = new HBox(6, actions, bubbleBox);
                row.setAlignment(Pos.CENTER_RIGHT);
                messagesVBox.getChildren().add(row);
            } else {
                bubble.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #0f172a; -fx-background-radius: 14 14 14 2; -fx-border-color: #e7ecf2; -fx-border-radius: 14 14 14 2; -fx-padding: 9 13;");
                bubbleBox.getChildren().addAll(bubble, timeLabel);

                if (message.isEdited()) {
                    Label editedTag = new Label("✏️ ویرایش شده");
                    editedTag.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 9px; -fx-font-style: italic; -fx-padding: 0 4;");
                    bubbleBox.getChildren().add(editedTag);
                }

                HBox row = new HBox(bubbleBox);
                row.setAlignment(Pos.CENTER_LEFT);
                messagesVBox.getChildren().add(row);
            }
        }
    }

    private void startEditMessage(ChatMessage message) {
        TextInputDialog dialog = new TextInputDialog(message.getText());
        dialog.setTitle("ویرایش پیام");
        dialog.setHeaderText(null);
        dialog.setContentText("متن جدید:");
        try { dialog.getDialogPane().getStylesheets().add(getClass().getResource(Routes.STYLESHEET).toExternalForm()); } catch (Exception ignored) { FrontendErrorHandler.log(ignored); }
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

    private void deleteMessage(ChatMessage message) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("حذف پیام");
        confirm.setHeaderText(null);
        confirm.setContentText("از حذف این پیام اطمینان دارید؟");
        try { confirm.getDialogPane().getStylesheets().add(getClass().getResource(Routes.STYLESHEET).toExternalForm()); } catch (Exception ignored) { FrontendErrorHandler.log(ignored); }
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

        if (currentConversation.isOtherPartyBlocked()) {
            showAlert("🔒 شما یا طرف مقابل مسدود شده‌اید و امکان ارسال پیام وجود ندارد!", Alert.AlertType.WARNING);
            return;
        }

        messageField.clear();
        final Long conversationId = currentConversation.getId();
        new Thread(() -> {
            try {
                ChatService.sendMessage(conversationId, text);
                List<ChatMessage> messages = ChatService.getMessages(conversationId);
                Platform.runLater(() -> { renderMessages(messages); scrollToBottom(); lastMessageCount = messages.size(); });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    String msg = e.getMessage();
                    if (msg != null && (msg.contains("مسدود") || msg.contains("blocked"))) {
                        showAlert("🔒 " + msg, Alert.AlertType.WARNING);
                    } else {
                        showAlert("خطا در ارسال پیام: " + msg, Alert.AlertType.ERROR);
                    }
                });
            }
        }).start();
    }

    @FXML
    private void goBack() {
        stopPolling();
        try { MainApplication.changeScene(Routes.AD_LIST, "لیست آگهی‌ها"); }
        catch (Exception e) { FrontendErrorHandler.log(e); }
    }

    private void startPolling() {
        refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(POLL_SECONDS), event -> {
            if (currentConversation != null) loadMessages(false);
            loadConversations();
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