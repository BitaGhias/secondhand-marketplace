package com.secondhand.backend.repository;

import com.secondhand.backend.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Spring Data JPA repository for {@code ChatMessage} entities.
 * <p>
 * This interface performs read and write operations on the database via Spring Data JPA; method implementations are generated at runtime from the method name or the {@code @Query} annotation.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    //  گرفتن تمام پیام‌های یک مکالمه به ترتیب قدیمی به جدید
    /**
     * Finds by conversation id order by timestamp asc.
     *
     * @param conversationId id of the conversation
     * @return a {@code List<ChatMessage>} with the results; empty if nothing matches
     */
    List<ChatMessage> findByConversationIdOrderByTimestampAsc(Long conversationId);
    // فقط پیام‌هایی که حذف نشده‌اند
    /**
     * Finds by conversation id and is deleted false order by timestamp asc.
     *
     * @param conversationId id of the conversation
     * @return a {@code List<ChatMessage>} with the results; empty if nothing matches
     */
    List<ChatMessage> findByConversationIdAndIsDeletedFalseOrderByTimestampAsc(Long conversationId);
    // تعداد پیام‌های خوانده‌نشده برای یک کاربر در یک مکالمه
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.conversation.id = :conversationId " +
            "AND m.sender.id != :userId AND m.isRead = false AND m.isDeleted = false")
    /**
     * Counts unread messages.
     *
     * @param conversationId id of the conversation
     * @param userId id of the user
     * @return the resulting numeric value
     */
    long countUnreadMessages(@Param("conversationId") Long conversationId,
                             @Param("userId") Long userId);

    // علامت‌گذاری همه پیام‌های یک مکالمه به عنوان خوانده‌شده
    @Modifying // کوئری رو تغییر میده
    @Transactional // مطمئن شو کامل انجام شه یا برگرده
    @Query("UPDATE ChatMessage m SET m.isRead = true " +
            "WHERE m.conversation.id = :conversationId AND m.sender.id != :userId AND m.isRead = false")
    /**
     * Marks all as read.
     *
     * @param conversationId id of the conversation
     * @param userId id of the user
     */
    void markAllAsRead(@Param("conversationId") Long conversationId,
                       @Param("userId") Long userId);
}