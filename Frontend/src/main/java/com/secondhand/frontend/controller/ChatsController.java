package com.secondhand.frontend.controller;

import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.model.ChatMessage;
import com.secondhand.frontend.model.Conversation;
import com.secondhand.frontend.service.ChatService;
import com.secondhand.frontend.util.SessionManager;
import com.secondhand.frontend.util.WindowUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class ChatsController extends BaseController {
    @FXML private ListView<Conversation> chatUsersListView;
    @FXML private Label currentChatUserLabel;
    @FXML private TextField messageField;
    @FXML private VBox messagesVBox;
    @FXML private HBox titleBar;

    private Conversation currentConversation;

    // گفت‌وگویی که باید بعد از باز شدن صفحه به صورت خودکار انتخاب شود
    // (وقتی از دکمه «پیام به فروشنده» در صفحه جزئیات آگهی می‌آییم)
    private static Long initialConversationId;

    public static void setInitialConversationId(Long conversationId) {
        initialConversationId = conversationId;
    }

    @FXML
    public void initialize() {
        WindowUtil.makeDraggable(titleBar);
        setupConversationList();
        loadConversations();
    }

    private void setupConversationList() {
        Long myId = SessionManager.getCurrentUserId();

        chatUsersListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Conversation conversation, boolean empty) {
                super.updateItem(conversation, empty);
                if (empty || conversation == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    String other = conversation.getOtherPartyUsername(myId);
                    setText("📦 " + conversation.getItemTitle() + "\n👤 " + (other != null ? other : "کاربر"));
                    setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 10;");
                }
            }
        });

        chatUsersListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                openConversation(newVal);
            }
        });
    }

    private void loadConversations() {
        new Thread(() -> {
            try {
                List<Conversation> conversations = ChatService.getConversations();
                Platform.runLater(() -> {
                    chatUsersListView.getItems().setAll(conversations);

                    // ✅ انتخاب خودکار گفت‌وگویی که از صفحه جزئیات آگهی آمده
                    if (initialConversationId != null) {
                        for (Conversation c : conversations) {
                            if (initialConversationId.equals(c.getId())) {
                                chatUsersListView.getSelectionModel().select(c);
                                chatUsersListView.scrollTo(c);
                                break;
                            }
                        }
                        initialConversationId = null;
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
        currentChatUserLabel.setText("💬 " + conversation.getItemTitle() + " — " + (other != null ? other : "کاربر"));
        loadMessages();
    }

    private void loadMessages() {
        if (currentConversation == null) return;
        final Long conversationId = currentConversation.getId();
        new Thread(() -> {
            try {
                List<ChatMessage> messages = ChatService.getMessages(conversationId);
                Platform.runLater(() -> renderMessages(messages));
            } catch (Exception e) {
                Platform.runLater(() -> {
                    messagesVBox.getChildren().clear();
                    Label errorLabel = new Label("خطا در دریافت پیام‌ها: " + e.getMessage());
                    errorLabel.setStyle("-fx-text-fill: #ff4757;");
                    messagesVBox.getChildren().add(errorLabel);
                });
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
            if (mine) {
                bubble.setStyle("-fx-background-color: #11998e; -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 8 12;");
            } else {
                bubble.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 8 12;");
            }

            HBox row = new HBox(bubble);
            // کانتینر RTL است: پیام من یک طرف، پیام طرف مقابل طرف دیگر
            row.setAlignment(mine ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);
            messagesVBox.getChildren().add(row);
        }
    }

    @FXML
    private void sendMessage() {
        if (currentConversation == null) {
            currentChatUserLabel.setText("ابتدا یک گفت‌وگو را انتخاب کنید");
            return;
        }
        String text = messageField.getText() != null ? messageField.getText().trim() : "";
        if (text.isEmpty()) return;

        messageField.clear();
        final Long conversationId = currentConversation.getId();
        new Thread(() -> {
            try {
                ChatService.sendMessage(conversationId, text);
                List<ChatMessage> messages = ChatService.getMessages(conversationId);
                Platform.runLater(() -> renderMessages(messages));
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Label errorLabel = new Label("خطا در ارسال پیام: " + e.getMessage());
                    errorLabel.setStyle("-fx-text-fill: #ff4757;");
                    messagesVBox.getChildren().add(errorLabel);
                });
            }
        }).start();
    }

    @FXML
    private void goBack() {
        try {
            MainApplication.changeScene("/com/secondhand/frontend/adlist.fxml", "لیست آگهی‌ها");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
