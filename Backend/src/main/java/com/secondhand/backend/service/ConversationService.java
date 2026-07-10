package com.secondhand.backend.service;

import com.secondhand.backend.dto.*;
import com.secondhand.backend.entity.*;
import com.secondhand.backend.exception.custom.BadRequestException;
import com.secondhand.backend.exception.custom.ForbiddenException;
import com.secondhand.backend.exception.custom.ResourceNotFoundException;
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
    private ConversationRepository conversationRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private ConversationResponse convertToConversationResponse(Conversation conv) {
        return new ConversationResponse(
                conv.getId(),
                conv.getItem() != null ? conv.getItem().getId() : null,
                conv.getItem() != null ? conv.getItem().getTitle() : "آگهی حذف شده",
                conv.getBuyer() != null ? conv.getBuyer().getId() : null,
                conv.getBuyer() != null ? conv.getBuyer().getUsername() : "ناشناس",
                conv.getSeller() != null ? conv.getSeller().getId() : null,
                conv.getSeller() != null ? conv.getSeller().getUsername() : "ناشناس"
        );
    }

    private ChatMessageResponse convertToMessageResponse(ChatMessage msg) {
        return new ChatMessageResponse(
                msg.getId(),
                msg.getConversation() != null ? msg.getConversation().getId() : null,
                msg.getSender() != null ? msg.getSender().getId() : null,
                msg.getSender() != null ? msg.getSender().getUsername() : "ناشناس",
                msg.getText(),
                msg.getTimestamp()
        );
    }

    public ConversationResponse startConversation(Long itemId, Long buyerId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("آگهی یافت نشد"));
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("خریدار یافت نشد"));
        User seller = item.getUser();

        if (seller.getId().equals(buyerId)) {
            throw new BadRequestException("شما نمی‌توانید با خودتان روی آگهی خودتان چت کنید!");
        }

        Optional<Conversation> existing = conversationRepository.findByBuyerIdAndSellerIdAndItemId(buyerId, seller.getId(), itemId);
        if (existing.isPresent()) {
            return convertToConversationResponse(existing.get());
        }

        Conversation conversation = new Conversation();
        conversation.setItem(item);
        conversation.setBuyer(buyer);
        conversation.setSeller(seller);

        Conversation saved = conversationRepository.save(conversation);
        return convertToConversationResponse(saved);
    }

    public ChatMessageResponse sendMessage(ChatMessageRequest request, Long senderId) {
        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new ResourceNotFoundException("مکالمه یافت نشد"));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("فرستنده یافت نشد"));

        if (!conversation.getBuyer().getId().equals(senderId) && !conversation.getSeller().getId().equals(senderId)) {
            throw new ForbiddenException("شما عضو این مکالمه نیستید!");
        }

        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setText(request.getText());
        message.setTimestamp(LocalDateTime.now());

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