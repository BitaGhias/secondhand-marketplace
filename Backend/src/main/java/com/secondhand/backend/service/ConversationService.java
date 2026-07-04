package com.secondhand.backend.service;

import com.secondhand.backend.entity.ChatMessage;
import com.secondhand.backend.entity.Conversation;
import com.secondhand.backend.entity.Item;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.repository.ChatMessageRepository;
import com.secondhand.backend.repository.ConversationRepository;
import com.secondhand.backend.repository.ItemRepository;
import com.secondhand.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
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

    public Conversation startConversation(Long itemId, Long buyerId) {
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
            return existing.get();
        }

        Conversation conversation = new Conversation();
        conversation.item = item;
        conversation.buyer = buyer;
        conversation.seller = seller;

        return conversationRepository.save(conversation);
    }

    public ChatMessage sendMessage(Long conversationId, Long senderId, String text) {

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("مکالمه یافت نشد"));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("فرستنده یافت نشد"));

        // بررسی اینکه فرستنده حتما خریدار یا فروشنده همین مکالمه باشد
        if (!conversation.buyer.getId().equals(senderId) && !conversation.seller.getId().equals(senderId)) {
            throw new RuntimeException("شما عضو این مکالمه نیستید!");
        }

        ChatMessage message = new ChatMessage();
        message.conversation = conversation;
        message.sender = sender;
        message.text = text;
        message.timestamp = LocalDateTime.now();

        return chatMessageRepository.save(message);
    }

    // دریافت لیست پیام های داخل یک مکالمه
    public List<ChatMessage> getMessages(Long conversationId) {
        return chatMessageRepository.findByConversationIdOrderByTimestampAsc(conversationId);
    }

    //  دریافت تمام مکالمات یک کاربر
    public List<Conversation> getUserConversations(Long userId) {
        return conversationRepository.findByBuyerIdOrSellerId(userId, userId);
    }
}