package com.secondhand.backend.repository;

import com.secondhand.backend.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    //  گرفتن تمام پیام‌های یک مکالمه به ترتیب قدیمی به جدید
    List<ChatMessage> findByConversationIdOrderByTimestampAsc(Long conversationId);
}