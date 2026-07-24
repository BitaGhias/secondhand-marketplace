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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Business-logic service for "conversation" operations.
 * <p>
 * This class implements the core business logic and sits between the controller layer and the repository layer. Validation and access control are enforced here and a proper exception is thrown when a rule is violated.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
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

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    /**
     * Converts to conversation response.
     *
     * @param conv the "conv" value of type {@code Conversation}
     * @param userId id of the user
     * @return the resulting {@code ConversationResponse} instance
     */
    private ConversationResponse convertToConversationResponse(Conversation conv, Long userId) {
        List<ChatMessage> messages = chatMessageRepository
                .findByConversationIdAndIsDeletedFalseOrderByTimestampAsc(conv.getId());

        String lastMessage = null;
        LocalDateTime lastMessageTime = null;
        String lastMessageSender = null;
        if (!messages.isEmpty()) {
            ChatMessage last = messages.get(messages.size() - 1);
            lastMessage = last.getText();
            lastMessageTime = last.getTimestamp();
            lastMessageSender = last.getSender().getUsername();
        }

        long unreadCount = chatMessageRepository.countUnreadMessages(conv.getId(), userId);

        User otherUser = conv.getBuyer().getId().equals(userId) ? conv.getSeller() : conv.getBuyer();

        return new ConversationResponse(
                conv.getId(),
                conv.getItem() != null ? conv.getItem().getId() : null,
                conv.getItem() != null ? conv.getItem().getTitle() : "آگهی حذف شده",
                conv.getBuyer() != null ? conv.getBuyer().getId() : null,
                conv.getBuyer() != null ? conv.getBuyer().getUsername() : "ناشناس",
                conv.getSeller() != null ? conv.getSeller().getId() : null,
                conv.getSeller() != null ? conv.getSeller().getUsername() : "ناشناس",
                otherUser.getId(),
                otherUser.getUsername(),
                otherUser.isBlocked(),
                lastMessage,
                lastMessageTime != null ? lastMessageTime.format(DateTimeFormatter.ofPattern("HH:mm")) : null,
                lastMessageTime != null ? lastMessageTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")) : null,
                lastMessageSender,
                unreadCount
        );
    }

    /**
     * Converts to message response.
     *
     * @param msg the "msg" value of type {@code ChatMessage}
     * @return the resulting {@code ChatMessageResponse} instance
     */
    private ChatMessageResponse convertToMessageResponse(ChatMessage msg) {
        return new ChatMessageResponse(
                msg.getId(),
                msg.getConversation() != null ? msg.getConversation().getId() : null,
                msg.getSender() != null ? msg.getSender().getId() : null,
                msg.getSender() != null ? msg.getSender().getUsername() : "ناشناس",
                msg.isDeleted() ? "این پیام حذف شده است" : msg.getText(),
                msg.getTimestamp(),
                msg.getTimestamp() != null ? msg.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm")) : null,
                msg.getTimestamp() != null ? msg.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")) : null,
                msg.isRead(),
                msg.isDeleted(),
                msg.isEdited()
        );
    }

    /**
     * Validates conversation membership.
     *
     * @param conversation the conversation object
     * @param userId id of the user
     */
    private void validateConversationMembership(Conversation conversation, Long userId) {
        boolean isBuyer = conversation.getBuyer().getId().equals(userId);
        boolean isSeller = conversation.getSeller().getId().equals(userId);
        if (!isBuyer && !isSeller) {
            throw new ForbiddenException("شما عضو این مکالمه نیستید!");
        }
    }

    /**
     * Starts conversation.
     *
     * @param itemId id of the ad (item)
     * @param buyerId id of the buyer
     * @return the resulting {@code ConversationResponse} instance
     */
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

    /**
     * Sends message.
     *
     * @param request request body received from the client
     * @param senderId the "sender id" value of type {@code Long}
     * @return the resulting {@code ChatMessageResponse} instance
     */
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

        if (otherUser.isBlocked() || sender.isBlocked()) {
            throw new ForbiddenException("شما یا طرف مقابل مسدود شده‌اید و امکان ارسال پیام وجود ندارد!");
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
     * Gets messages.
     *
     * @param conversationId id of the conversation
     * @param userId id of the user
     * @return a {@code List<ChatMessageResponse>} with the results; empty if nothing matches
     */
    public List<ChatMessageResponse> getMessages(Long conversationId, Long userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("مکالمه یافت نشد"));

        validateConversationMembership(conversation, userId);

        chatMessageRepository.markAllAsRead(conversationId, userId);

        List<ChatMessage> messages = chatMessageRepository
                .findByConversationIdAndIsDeletedFalseOrderByTimestampAsc(conversationId);
        List<ChatMessageResponse> responses = new ArrayList<>();
        for (ChatMessage msg : messages) {
            responses.add(convertToMessageResponse(msg));
        }
        return responses;
    }

    /**
     * Gets user conversations.
     *
     * @param userId id of the user
     * @return a {@code List<ConversationResponse>} with the results; empty if nothing matches
     */
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

    /**
     * Edits message.
     *
     * @param messageId the "message id" value of type {@code Long}
     * @param userId id of the user
     * @param newText the "new text" value of type {@code String}
     * @return the resulting {@code ChatMessageResponse} instance
     */
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

    /**
     * Deletes message.
     *
     * @param messageId the "message id" value of type {@code Long}
     * @param userId id of the user
     * @return the resulting {@code ChatMessageResponse} instance
     */
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

    /**
     * Gets last message.
     *
     * @param conversationId id of the conversation
     * @return the resulting {@code ChatMessageResponse} instance
     */
    public ChatMessageResponse getLastMessage(Long conversationId) {
        List<ChatMessage> messages = chatMessageRepository
                .findByConversationIdAndIsDeletedFalseOrderByTimestampAsc(conversationId);
        if (messages.isEmpty()) {
            return null;
        }
        return convertToMessageResponse(messages.get(messages.size() - 1));
    }
}