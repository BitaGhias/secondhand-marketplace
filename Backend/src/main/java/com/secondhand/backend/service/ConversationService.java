package com.secondhand.backend.service;

import com.secondhand.backend.constant.ItemStatus;
import com.secondhand.backend.dto.chat.ChatMessageRequest;
import com.secondhand.backend.dto.chat.ChatMessageResponse;
import com.secondhand.backend.dto.chat.ConversationResponse;
import com.secondhand.backend.entity.*;
import com.secondhand.backend.exception.custom.BadRequestException;
import com.secondhand.backend.exception.custom.ForbiddenException;
import com.secondhand.backend.exception.custom.ResourceNotFoundException;
import com.secondhand.backend.repository.*;
import com.secondhand.backend.util.UserValidationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    private ConversationResponse convertToConversationResponse(Conversation conv, Long userId) {
        List<ChatMessage> messages = chatMessageRepository
                .findByConversationIdAndIsDeletedFalseOrderByTimestampAsc(conv.getId());

        String lastMessage = null;
        LocalDateTime lastMessageTime = null;
        if (!messages.isEmpty()) {
            ChatMessage last = messages.get(messages.size() - 1);
            lastMessage = last.getText();
            lastMessageTime = last.getTimestamp();
        }

        long unreadCount = chatMessageRepository.countUnreadMessages(conv.getId(), userId);

        return new ConversationResponse(
                conv.getId(),
                conv.getItem() != null ? conv.getItem().getId() : null,
                conv.getItem() != null ? conv.getItem().getTitle() : "آگهی حذف شده",
                conv.getBuyer() != null ? conv.getBuyer().getId() : null,
                conv.getBuyer() != null ? conv.getBuyer().getUsername() : "ناشناس",
                conv.getSeller() != null ? conv.getSeller().getId() : null,
                conv.getSeller() != null ? conv.getSeller().getUsername() : "ناشناس",
                lastMessage,
                lastMessageTime,
                unreadCount
        );
    }

    private ChatMessageResponse convertToMessageResponse(ChatMessage msg) {
        return new ChatMessageResponse(
                msg.getId(),
                msg.getConversation() != null ? msg.getConversation().getId() : null,
                msg.getSender() != null ? msg.getSender().getId() : null,
                msg.getSender() != null ? msg.getSender().getUsername() : "ناشناس",
                msg.isDeleted() ? "این پیام حذف شده است" : msg.getText(),
                msg.getTimestamp(),
                msg.isRead(),
                msg.isDeleted(),
                msg.isEdited()
        );
    }

    // بررسی عضویت کاربر در مکالمه
    private void validateConversationMembership(Conversation conversation, Long userId) {
        boolean isBuyer = conversation.getBuyer().getId().equals(userId);
        boolean isSeller = conversation.getSeller().getId().equals(userId);
        if (!isBuyer && !isSeller) {
            throw new ForbiddenException("شما عضو این مکالمه نیستید!");
        }
    }

    public ConversationResponse startConversation(Long itemId, Long buyerId) {
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("خریدار یافت نشد"));
        UserValidationHelper.validateActiveAndNotBlocked(buyer);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("آگهی یافت نشد"));

        if (item.getStatus() != ItemStatus.APPROVED) {
            throw new BadRequestException("این آگهی قابل چت کردن نیست!");
        }

        User seller = item.getUser();

        if (seller.getId().equals(buyerId)) {
            throw new BadRequestException("شما نمی‌توانید با خودتان روی آگهی خودتان چت کنید!");
        }

        UserValidationHelper.validateActiveAndNotBlocked(seller);

        Optional<Conversation> existing = conversationRepository
                .findByBuyerIdAndSellerIdAndItemId(buyerId, seller.getId(), itemId);
        if (existing.isPresent()) {
            return convertToConversationResponse(existing.get(), buyerId);
        }

        Conversation conversation = new Conversation();
        conversation.setItem(item);
        conversation.setBuyer(buyer);
        conversation.setSeller(seller);

        Conversation saved = conversationRepository.save(conversation);
        return convertToConversationResponse(saved, buyerId);
    }

    @Transactional
    public ChatMessageResponse sendMessage(ChatMessageRequest request, Long senderId) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("فرستنده یافت نشد"));
        UserValidationHelper.validateActiveAndNotBlocked(sender);

        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new ResourceNotFoundException("مکالمه یافت نشد"));

        validateConversationMembership(conversation, senderId);

        User otherUser = conversation.getBuyer().getId().equals(senderId)
                ? conversation.getSeller()
                : conversation.getBuyer();
        if (otherUser.isBlocked()) {
            throw new ForbiddenException("کاربر مقابل شما مسدود شده است!");
        }

        if (request.getText() == null || request.getText().trim().isEmpty()) {
            throw new BadRequestException("متن پیام نمی‌تواند خالی باشد!");
        }

        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setText(request.getText());
        message.setTimestamp(LocalDateTime.now());
        message.setRead(false);
        message.setDeleted(false);
        message.setEdited(false);

        ChatMessage saved = chatMessageRepository.save(message);
        return convertToMessageResponse(saved);
    }

    /**
     *  FIX: بررسی عضویت کاربر در مکالمه قبل از نمایش پیام‌ها
     *         قبلاً هر کاربری می‌توانست پیام هر مکالمه‌ای را بخواند
     */
    public List<ChatMessageResponse> getMessages(Long conversationId, Long userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("مکالمه یافت نشد"));

        validateConversationMembership(conversation, userId);  //  چک عضویت اضافه شد

        chatMessageRepository.markAllAsRead(conversationId, userId);

        List<ChatMessage> messages = chatMessageRepository
                .findByConversationIdAndIsDeletedFalseOrderByTimestampAsc(conversationId);
        List<ChatMessageResponse> responses = new ArrayList<>();
        for (ChatMessage msg : messages) {
            responses.add(convertToMessageResponse(msg));
        }
        return responses;
    }

    public List<ConversationResponse> getUserConversations(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("کاربر یافت نشد");
        }

        List<Conversation> conversations = conversationRepository
                .findByBuyerIdOrSellerId(userId, userId);
        List<ConversationResponse> responses = new ArrayList<>();
        for (Conversation conv : conversations) {
            responses.add(convertToConversationResponse(conv, userId));
        }
        return responses;
    }

    @Transactional
    public ChatMessageResponse editMessage(Long messageId, Long userId, String newText) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("پیام یافت نشد"));

        if (!message.getSender().getId().equals(userId)) {
            throw new ForbiddenException("شما اجازه ویرایش این پیام را ندارید!");
        }

        if (message.isDeleted()) {
            throw new BadRequestException("این پیام حذف شده است و قابل ویرایش نیست!");
        }

        if (newText == null || newText.trim().isEmpty()) {
            throw new BadRequestException("متن پیام نمی‌تواند خالی باشد!");
        }

        message.setText(newText);
        message.setEdited(true);
        ChatMessage updated = chatMessageRepository.save(message);
        return convertToMessageResponse(updated);
    }

    @Transactional
    public ChatMessageResponse deleteMessage(Long messageId, Long userId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("پیام یافت نشد"));

        if (!message.getSender().getId().equals(userId)) {
            throw new ForbiddenException("شما اجازه حذف این پیام را ندارید!");
        }

        if (message.isDeleted()) {
            throw new BadRequestException("این پیام قبلاً حذف شده است!");
        }

        message.setDeleted(true);
        ChatMessage deleted = chatMessageRepository.save(message);
        return convertToMessageResponse(deleted);
    }

    public ChatMessageResponse getLastMessage(Long conversationId) {
        List<ChatMessage> messages = chatMessageRepository
                .findByConversationIdAndIsDeletedFalseOrderByTimestampAsc(conversationId);
        if (messages.isEmpty()) {
            return null;
        }
        return convertToMessageResponse(messages.get(messages.size() - 1));
    }
}