package com.secondhand.backend.repository;

import com.secondhand.backend.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    //  پیدا کردن تمام مکالماتی که یک کاربر در آن خریدار یا فروشنده است (برای منوی چت‌های کاربر)
    List<Conversation> findByBuyerIdOrSellerId(Long buyerId, Long sellerId);

    //  پیدا کردن مکالمه بین یک خریدار و فروشنده خاص روی یک آگهی معین (برای جلوگیری از ساخت چت تکراری)
    Optional<Conversation> findByBuyerIdAndSellerIdAndItemId(Long buyerId, Long sellerId, Long itemId);
}