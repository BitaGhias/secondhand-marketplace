package com.secondhand.frontend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.frontend.model.Conversation;
import com.secondhand.frontend.model.ChatMessage;
import com.secondhand.frontend.util.ApiClient;

import java.net.http.HttpResponse;
import java.util.List;

/**
 * Client-side service for "chat" operations against the backend API.
 * <p>
 * This class is the client-to-server communication layer; it sends requests to the backend API through {@code ApiClient} and converts JSON responses into Java models with Jackson. On a non-successful response the server error message is propagated as an exception.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class ChatService {

    private static final ObjectMapper objectMapper = ApiClient.getMapper();

    /**
     * Extracts the error message from the "message" field of the server JSON response; falls back to a generic message with the status code.
     *
     * @param response the received response
     * @return the resulting string
     */
    private static String extractErrorMessage(HttpResponse<String> response) {
        try {
            JsonNode json = objectMapper.readTree(response.body());
            if (json.has("message")) {
                return json.get("message").asText();
            }
        } catch (Exception ignored) {}
        return "خطای ناشناخته از سرور (کد: " + response.statusCode() + ")";
    }

    /**
     * Returns all conversations of the current user together with the last message and the unread-message count of each conversation.
     *
     * @return a {@code List<Conversation>} with the results; empty if nothing matches
     * @throws Exception if the request fails or the server cannot be reached
     */
    public static List<Conversation> getConversations() throws Exception {
        HttpResponse<String> response = ApiClient.get("/chat/user");
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Conversation>>() {});
        } else {
            throw new Exception(extractErrorMessage(response));
        }
    }

    /**
     * Gets messages.
     *
     * @param conversationId id of the conversation
     * @return a {@code List<ChatMessage>} with the results; empty if nothing matches
     * @throws Exception if the request fails or the server cannot be reached
     */
    public static List<ChatMessage> getMessages(Long conversationId) throws Exception {
        HttpResponse<String> response = ApiClient.get("/chat/messages/" + conversationId);
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<ChatMessage>>() {});
        } else {
            throw new Exception(extractErrorMessage(response));
        }
    }

    /**
     * Sends message.
     *
     * @param conversationId id of the conversation
     * @param text the text value
     * @return the resulting {@code ChatMessage} instance
     * @throws Exception if the request fails or the server cannot be reached
     */
    public static ChatMessage sendMessage(Long conversationId, String text) throws Exception {
        SendMessageRequest request = new SendMessageRequest(conversationId, text);
        HttpResponse<String> response = ApiClient.post("/chat/message", request);
        if (response.statusCode() == 200 || response.statusCode() == 201) {
            return objectMapper.readValue(response.body(), ChatMessage.class);
        } else {
            throw new Exception(extractErrorMessage(response));
        }
    }

    /**
     * Starts conversation.
     *
     * @param itemId id of the ad (item)
     * @param firstMessage the "first message" value of type {@code String}
     * @return the resulting {@code Conversation} instance
     * @throws Exception if the request fails or the server cannot be reached
     */
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

    /**
     * Edits message.
     *
     * @param messageId the "message id" value of type {@code Long}
     * @param newText the "new text" value of type {@code String}
     * @return the resulting {@code ChatMessage} instance
     * @throws Exception if the request fails or the server cannot be reached
     */
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

    /**
     * Deletes message.
     *
     * @param messageId the "message id" value of type {@code Long}
     * @return the resulting {@code ChatMessage} instance
     * @throws Exception if the request fails or the server cannot be reached
     */
    public static ChatMessage deleteMessage(Long messageId) throws Exception {
        HttpResponse<String> response = ApiClient.delete("/chat/message/" + messageId);
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), ChatMessage.class);
        } else {
            throw new Exception(extractErrorMessage(response));
        }
    }

    /**
     * Nested class used by {@code ChatService}.
     */
    public static class SendMessageRequest {
        public Long conversationId;
        public String text;
        /**
         * Sends message request.
         *
         * @param conversationId id of the conversation
         * @param text the text value
         */
        public SendMessageRequest(Long conversationId, String text) {
            this.conversationId = conversationId;
            this.text = text;
        }
    }
}