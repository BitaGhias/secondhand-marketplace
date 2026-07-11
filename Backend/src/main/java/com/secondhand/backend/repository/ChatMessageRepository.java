package com.secondhand.backend.repository;

import com.secondhand.backend.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    //  گرفتن تمام پیام‌های یک مکالمه به ترتیب قدیمی به جدید
    List<ChatMessage> findByConversationIdOrderByTimestampAsc(Long conversationId);
    // فقط پیام‌هایی که حذف نشده‌اند
    List<ChatMessage> findByConversationIdAndIsDeletedFalseOrderByTimestampAsc(Long conversationId);
    // تعداد پیام‌های خوانده‌نشده برای یک کاربر در یک مکالمه
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.conversation.id = :conversationId " +
            "AND m.sender.id != :userId AND m.isRead = false AND m.isDeleted = false")
    long countUnreadMessages(@Param("conversationId") Long conversationId,
                             @Param("userId") Long userId);

    // علامت‌گذاری همه پیام‌های یک مکالمه به عنوان خوانده‌شده
    @Modifying
    @Transactional
    @Query("UPDATE ChatMessage m SET m.isRead = true " +
            "WHERE m.conversation.id = :conversationId AND m.sender.id != :userId AND m.isRead = false")
    void markAllAsRead(@Param("conversationId") Long conversationId,
                       @Param("userId") Long userId);
}