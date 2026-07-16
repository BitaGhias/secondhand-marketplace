package com.secondhand.frontend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.model.Conversation;
import com.secondhand.frontend.model.ChatMessage;
import com.secondhand.frontend.service.ApiClient;

import java.net.http.HttpResponse;
import java.util.List;

public class ChatService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // دریافت لیست گفت‌وگوها
    public static List<Conversation> getConversations() throws Exception {
        HttpResponse<String> response = ApiClient.get("/conversations");

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Conversation>>() {});
        } else {
            throw new Exception("خطا در دریافت گفت‌وگوها: " + response.body());
        }
    }

    // دریافت پیام‌های یک گفت‌وگو
    public static List<ChatMessage> getMessages(Long conversationId) throws Exception {
        HttpResponse<String> response = ApiClient.get("/conversations/" + conversationId + "/messages");

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<ChatMessage>>() {});
        } else {
            throw new Exception("خطا در دریافت پیام‌ها: " + response.body());
        }
    }

    // ارسال پیام جدید
    public static ChatMessage sendMessage(Long conversationId, String content) throws Exception {
        SendMessageRequest request = new SendMessageRequest(content);
        HttpResponse<String> response = ApiClient.post("/conversations/" + conversationId + "/messages", request);

        if (response.statusCode() == 201) {
            return objectMapper.readValue(response.body(), ChatMessage.class);
        } else {
            throw new Exception("خطا در ارسال پیام: " + response.body());
        }
    }

    // شروع گفت‌وگو جدید (از صفحه جزئیات آگهی)
    public static Conversation startConversation(Long itemId, String firstMessage) throws Exception {
        StartConversationRequest request = new StartConversationRequest(itemId, firstMessage);
        HttpResponse<String> response = ApiClient.post("/conversations", request);

        if (response.statusCode() == 201) {
            return objectMapper.readValue(response.body(), Conversation.class);
        } else {
            throw new Exception("خطا در شروع گفت‌وگو: " + response.body());
        }
    }

    public static class SendMessageRequest {
        public String content;
        public SendMessageRequest(String content) {
            this.content = content;
        }
    }

    public static class StartConversationRequest {
        public Long itemId;
        public String firstMessage;
        public StartConversationRequest(Long itemId, String firstMessage) {
            this.itemId = itemId;
            this.firstMessage = firstMessage;
        }
    }
}