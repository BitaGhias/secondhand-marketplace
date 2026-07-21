package com.secondhand.frontend.controller;

import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.model.ChatMessage;
import com.secondhand.frontend.model.Conversation;
import com.secondhand.frontend.service.ChatService;
import com.secondhand.frontend.util.SessionManager;
import com.secondhand.frontend.util.WindowUtil;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.event.ActionEvent;
import javafx.util.Duration;

import java.util.List;

public class ChatsController extends BaseController {

    @FXML private ListView<Conversation> chatUsersListView;
    @FXML private Label currentChatUserLabel;
    @FXML private TextField messageField;
    @FXML private VBox messagesVBox;
    @FXML private ScrollPane messagesScrollPane;
    @FXML private HBox titleBar;

    private Conversation currentConversation;

    /** گفت‌وگویی که باید بعد از باز شدن صفحه خودکار انتخاب شود */
    private static Long initialConversationId;

    /** Timeline برای polling خودکار پیام‌ها */
    private Timeline refreshTimeline;

    /** آخرین تعداد پیام‌های رندر شده (برای جلوگیری از scroll به پایین بیخود) */
    private int lastMessageCount = 0;

    /** فاصله زمانی refresh بر حسب ثانیه */
    private static final int POLL_SECONDS = 3;

    // ─────────────────────
    public static void setInitialConversationId(Long id) { initialConversationId = id; }

    // ─────────────────────
    @FXML
    public void initialize() {
        WindowUtil.makeDraggable(titleBar);
        setupConversationList();
        loadConversations();
        startPolling();
    }

    // ─────────────────────
    //  ستاپ لیست گفت‌وگوها
    // ─────────────────────
    private void setupConversationList() {
        Long myId = SessionManager.getCurrentUserId();

        chatUsersListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Conversation c, boolean empty) {
                super.updateItem(c, empty);
                if (empty || c == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    String other = c.getOtherPartyUsername(myId);
                    setText("\uD83D\uDCE6 " + c.getItemTitle()
                            + "\n\uD83D\uDC64 " + (other != null ? other : "کاربر"));
                    setStyle("-fx-background-color: transparent; -fx-text-fill: #1f2937;"
                            + " -fx-font-size: 13px; -fx-padding: 10;");
                }
            }
        });

        chatUsersListView.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, newVal) -> {
                    if (newVal != null) openConversation(newVal);
                });
    }

    // ─────────────────────
    //  بارگذاری لیست گفت‌وگوها
    // ─────────────────────
    private void loadConversations() {
        new Thread(() -> {
            try {
                List<Conversation> conversations = ChatService.getConversations();
                Platform.runLater(() -> {
                    Conversation selected = chatUsersListView.getSelectionModel().getSelectedItem();
                    chatUsersListView.getItems().setAll(conversations);

                    if (initialConversationId != null) {
                        // انتخاب خودکار گفت‌وگویی که از جزئیات آگهی آمدیم
                        for (Conversation c : conversations) {
                            if (initialConversationId.equals(c.getId())) {
                                chatUsersListView.getSelectionModel().select(c);
                                chatUsersListView.scrollTo(c);
                                break;
                            }
                        }
                        initialConversationId = null;
                    } else if (selected != null) {
                        // حفظ انتخاب فعلی بعد از به‌روزرسانی لیست
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
                Platform.runLater(() ->
                        currentChatUserLabel.setText("خطا در دریافت گفت‌وگوها"));
            }
        }).start();
    }

    // ─────────────────────
    //  باز کردن گفت‌وگو
    // ─────────────────────
    private void openConversation(Conversation conversation) {
        currentConversation = conversation;
        Long myId = SessionManager.getCurrentUserId();
        String other = conversation.getOtherPartyUsername(myId);
        currentChatUserLabel.setText("\uD83D\uDCAC " + conversation.getItemTitle()
                + " — " + (other != null ? other : "کاربر"));
        lastMessageCount = 0;   // reset برای scroll تا پایین
        loadMessages(true);
    }

    // ─────────────────────
    //  بارگذاری پیام‌ها
    // ─────────────────────
    /**
     * @param scrollToBottom  اگر true باشد همیشه scroll می‌کند،
     *                        اگر false فقط وقتی scroll می‌کند که پیام جدید آمده باشد
     */
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
                // polling ساکت شکست — خطا نمایش نمی‌دهیم تا تجربه کاربر خراب نشود
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
                    ? "-fx-background-color: #059669; -fx-text-fill: white;"
                    + " -fx-background-radius: 12; -fx-padding: 8 12;"
                    : "-fx-background-color: #e7ecf2; -fx-text-fill: #1f2937;"
                    + " -fx-background-radius: 12; -fx-padding: 8 12;");

            // RTL layout: پیام‌های خودم سمت راست، بقیه سمت چپ
            HBox row = new HBox(bubble);
            row.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
            messagesVBox.getChildren().add(row);
        }
    }

    /** scroll به آخرین پیام */
    private void scrollToBottom() {
        if (messagesScrollPane != null) {
            messagesScrollPane.layout();
            messagesScrollPane.setVvalue(1.0);
        }
    }

    // ─────────────────────
    //  ارسال پیام
    // ─────────────────────
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
                Platform.runLater(() -> {
                    renderMessages(messages);
                    scrollToBottom();
                    lastMessageCount = messages.size();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Label err = new Label("خطا در ارسال پیام: " + e.getMessage());
                    err.setStyle("-fx-text-fill: #dc2626;");
                    messagesVBox.getChildren().add(err);
                });
            }
        }).start();
    }

    // ─────────────────────
    //  بازگشت
    // ─────────────────────
    @FXML
    private void goBack() {
        stopPolling();
        try {
            MainApplication.changeScene("/com/secondhand/frontend/adlist.fxml", "لیست آگهی‌ها");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────
    //  Polling خودکار (Timeline)
    // ─────────────────────
    private void startPolling() {
        refreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(POLL_SECONDS), event -> {
                    if (currentConversation != null) {
                        loadMessages(false);
                    }
                })
        );
        refreshTimeline.setCycleCount(Animation.INDEFINITE);
        refreshTimeline.play();
    }

    private void stopPolling() {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
            refreshTimeline = null;
        }
    }

    // ─────────────────────
    //  Window controls — از BaseController به ارث می‌رسند
    //  closeWindow را override می‌کنیم تا polling هم متوقف شود
    // ─────────────────────
    @Override
    @FXML
    public void closeWindow(ActionEvent event) {
        stopPolling();
        super.closeWindow(event);
    }
}