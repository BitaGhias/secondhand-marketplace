package com.secondhand.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // متنی که فرستاده شده
    @Column(nullable = false)
    private String text;

    // زمان ارسال پیام
    @Column(nullable = false)
    private LocalDateTime timestamp;

    // این پیام متعلق به کدام اتاق مکالمه است؟
    @ManyToOne
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    // چه کسی این پیام را فرستاده؟ (خریدار یا فروشنده)
    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;
}
