package com.secondhand.frontend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.model.Conversation;
import com.secondhand.frontend.model.ChatMessage;
import com.secondhand.frontend.util.ApiClient;

import java.net.http.HttpResponse;
import java.util.List;

public class ChatService {

    private static final ObjectMapper objectMapper = ApiClient.getMapper();

    private static String extractErrorMessage(HttpResponse<String> response) {
        try {
            JsonNode json = objectMapper.readTree(response.body());
            if (json.has("message")) {
                return json.get("message").asText();
            }
        } catch (Exception ignored) {}
        return "خطای ناشناخته از سرور (کد: " + response.statusCode() + ")";
    }

    public static List<Conversation> getConversations() throws Exception {
        HttpResponse<String> response = ApiClient.get("/chat/user");
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Conversation>>() {});
        } else {
            throw new Exception(extractErrorMessage(response));
        }
    }

    public static List<ChatMessage> getMessages(Long conversationId) throws Exception {
        HttpResponse<String> response = ApiClient.get("/chat/messages/" + conversationId);
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<ChatMessage>>() {});
        } else {
            throw new Exception(extractErrorMessage(response));
        }
    }

    public static ChatMessage sendMessage(Long conversationId, String text) throws Exception {
        SendMessageRequest request = new SendMessageRequest(conversationId, text);
        HttpResponse<String> response = ApiClient.post("/chat/message", request);
        if (response.statusCode() == 200 || response.statusCode() == 201) {
            return objectMapper.readValue(response.body(), ChatMessage.class);
        } else {
            throw new Exception(extractErrorMessage(response));
        }
    }

    public static Conversation startConversation(Long itemId, String firstMessage) throws Exception {
        HttpResponse<String> response = ApiClient.post("/chat/conversation?itemId=" + itemId);
        if (response.statusCode() == 200 || response.statusCode() == 201) {
            Conversation conversation = objectMapper.readValue(response.body(), Conversation.class);
            if (firstMessage != null && !firstMessage.trim().isEmpty()) {
                sendMessage(conversation.getId(), firstMessage);
            }
            return conversation;
        } else {
            throw new Exception(extractErrorMessage(response));
        }
    }

    public static ChatMessage editMessage(Long messageId, String newText) throws Exception {
        HttpResponse<String> response = ApiClient.put(
                "/chat/message/" + messageId + "?text=" + java.net.URLEncoder.encode(newText, "UTF-8"),
                null
        );
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), ChatMessage.class);
        } else {
            throw new Exception(extractErrorMessage(response));
        }
    }

    public static ChatMessage deleteMessage(Long messageId) throws Exception {
        HttpResponse<String> response = ApiClient.delete("/chat/message/" + messageId);
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), ChatMessage.class);
        } else {
            throw new Exception(extractErrorMessage(response));
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