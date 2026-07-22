package com.secondhand.frontend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.model.Conversation;
import com.secondhand.frontend.model.ChatMessage;
import com.secondhand.frontend.util.ApiClient;

import java.net.http.HttpResponse;
import java.util.List;

public class ChatService {

    private static final ObjectMapper objectMapper = ApiClient.getMapper();

    // دریافت لیست گفت‌وگوهای کاربر جاری
    public static List<Conversation> getConversations() throws Exception {
        HttpResponse<String> response = ApiClient.get("/chat/user");

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Conversation>>() {});
        } else {
            throw new Exception("خطا در دریافت گفت‌وگوها: " + response.body());
        }
    }

    // دریافت پیام‌های یک گفت‌وگو
    public static List<ChatMessage> getMessages(Long conversationId) throws Exception {
        HttpResponse<String> response = ApiClient.get("/chat/messages/" + conversationId);

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<ChatMessage>>() {});
        } else {
            throw new Exception("خطا در دریافت پیام‌ها: " + response.body());
        }
    }

    // ارسال پیام جدید (مطابق بک‌اند: POST /api/chat/message با body {conversationId, text})
    public static ChatMessage sendMessage(Long conversationId, String text) throws Exception {
        SendMessageRequest request = new SendMessageRequest(conversationId, text);
        HttpResponse<String> response = ApiClient.post("/chat/message", request);

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            return objectMapper.readValue(response.body(), ChatMessage.class);
        } else {
            throw new Exception("خطا در ارسال پیام: " + response.body());
        }
    }

    // شروع گفت‌وگوی جدید (از صفحه جزئیات آگهی)
    // مطابق بک‌اند: POST /api/chat/conversation?itemId=X و سپس ارسال پیام اول
    public static Conversation startConversation(Long itemId, String firstMessage) throws Exception {
        HttpResponse<String> response = ApiClient.post("/chat/conversation?itemId=" + itemId);

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            Conversation conversation = objectMapper.readValue(response.body(), Conversation.class);
            if (firstMessage != null && !firstMessage.trim().isEmpty()) {
                sendMessage(conversation.getId(), firstMessage);
            }
            return conversation;
        } else {
            throw new Exception("خطا در شروع گفت‌وگو: " + response.body());
        }
    }

    // FIX: ویرایش پیام ارسالی (مطابق بک‌اند: PUT /api/chat/message/{id}?text=...) - قبلاً این قابلیت از فرانت قابل دسترس نبود
    public static ChatMessage editMessage(Long messageId, String newText) throws Exception {
        HttpResponse<String> response = ApiClient.put(
                "/chat/message/" + messageId + "?text=" + java.net.URLEncoder.encode(newText, "UTF-8"),
                null
        );
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), ChatMessage.class);
        } else {
            throw new Exception("خطا در ویرایش پیام: " + response.body());
        }
    }

    // FIX: حذف پیام ارسالی (مطابق بک‌اند: DELETE /api/chat/message/{id}) - قبلاً این قابلیت از فرانت قابل دسترس نبود
    public static ChatMessage deleteMessage(Long messageId) throws Exception {
        HttpResponse<String> response = ApiClient.delete("/chat/message/" + messageId);
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), ChatMessage.class);
        } else {
            throw new Exception("خطا در حذف پیام: " + response.body());
        }
    }

    public static class SendMessageRequest {
        public Long conversationId;
        public String text;
        public SendMessageRequest(Long conversationId, String text) {
            this.conversationId = conversationId;
            this.text = text;
        }
    }
}