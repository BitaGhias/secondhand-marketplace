package com.secondhand.backend.service;

import com.secondhand.backend.dto.*;
import com.secondhand.backend.entity.*;
import com.secondhand.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ConversationService {

    @Autowired
    public ConversationRepository conversationRepository;
    @Autowired
    public ChatMessageRepository chatMessageRepository;
    @Autowired
    public UserRepository userRepository;
    @Autowired
    public ItemRepository itemRepository;

    private ConversationResponse convertToConversationResponse(Conversation conv) {
        return new ConversationResponse(
                conv.id,
                conv.item != null ? conv.item.getId() : null,
                conv.item != null ? conv.item.getTitle() : "آگهی حذف شده",
                conv.buyer != null ? conv.buyer.getId() : null,
                conv.buyer != null ? conv.buyer.getUsername() : "ناشناس",
                conv.seller != null ? conv.seller.getId() : null,
                conv.seller != null ? conv.seller.getUsername() : "ناشناس"
        );
    }

    private ChatMessageResponse convertToMessageResponse(ChatMessage msg) {
        return new ChatMessageResponse(
                msg.id,
                msg.conversation != null ? msg.conversation.id : null,
                msg.sender != null ? msg.sender.getId() : null,
                msg.sender != null ? msg.sender.getUsername() : "ناشناس",
                msg.text,
                msg.timestamp
        );
    }

    public ConversationResponse startConversation(Long itemId, Long buyerId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("آگهی یافت نشد"));
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("خریدار یافت نشد"));
        User seller = item.getUser();

        if (seller.getId().equals(buyerId)) {
            throw new RuntimeException("شما نمی‌توانید با خودتان روی آگهی خودتان چت کنید!");
        }

        Optional<Conversation> existing = conversationRepository.findByBuyerIdAndSellerIdAndItemId(buyerId, seller.getId(), itemId);
        if (existing.isPresent()) {
            return convertToConversationResponse(existing.get());
        }

        Conversation conversation = new Conversation();
        conversation.item = item;
        conversation.buyer = buyer;
        conversation.seller = seller;

        Conversation saved = conversationRepository.save(conversation);
        return convertToConversationResponse(saved);
    }

    public ChatMessageResponse sendMessage(ChatMessageRequest request) {
        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new RuntimeException("مکالمه یافت نشد"));
        User sender = userRepository.findById(request.getSenderId())
                .orElseThrow(() -> new RuntimeException("فرستنده یافت نشد"));

        if (!conversation.buyer.getId().equals(request.getSenderId()) && !conversation.seller.getId().equals(request.getSenderId())) {
            throw new RuntimeException("شما عضو این مکالمه نیستید!");
        }

        ChatMessage message = new ChatMessage();
        message.conversation = conversation;
        message.sender = sender;
        message.text = request.getText();
        message.timestamp = LocalDateTime.now();

        ChatMessage saved = chatMessageRepository.save(message);
        return convertToMessageResponse(saved);
    }

    public List<ChatMessageResponse> getMessages(Long conversationId) {
        List<ChatMessage> messages = chatMessageRepository.findByConversationIdOrderByTimestampAsc(conversationId);
        List<ChatMessageResponse> responses = new ArrayList<>();
        for (ChatMessage msg : messages) {
            responses.add(convertToMessageResponse(msg));
        }
        return responses;
    }

    public List<ConversationResponse> getUserConversations(Long userId) {
        List<Conversation> conversations = conversationRepository.findByBuyerIdOrSellerId(userId, userId);
        List<ConversationResponse> responses = new ArrayList<>();
        for (Conversation conv : conversations) {
            responses.add(convertToConversationResponse(conv));
        }
        return responses;
    }
}