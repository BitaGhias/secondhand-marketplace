package com.secondhand.backend.repository;

import com.secondhand.backend.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@code Conversation} entities.
 * <p>
 * This interface performs read and write operations on the database via Spring Data JPA; method implementations are generated at runtime from the method name or the {@code @Query} annotation.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    //  پیدا کردن تمام مکالماتی که یک کاربر در آن خریدار یا فروشنده است (برای منوی چت‌های کاربر)
    /**
     * Finds by buyer id or seller id.
     *
     * @param buyerId id of the buyer
     * @param sellerId id of the seller
     * @return a {@code List<Conversation>} with the results; empty if nothing matches
     */
    List<Conversation> findByBuyerIdOrSellerId(Long buyerId, Long sellerId);

    //  پیدا کردن مکالمه بین یک خریدار و فروشنده خاص روی یک آگهی معین (برای جلوگیری از ساخت چت تکراری)
    /**
     * Finds by buyer id and seller id and item id.
     *
     * @param buyerId id of the buyer
     * @param sellerId id of the seller
     * @param itemId id of the ad (item)
     * @return an {@code Optional<Conversation>} that may be empty
     */
    Optional<Conversation> findByBuyerIdAndSellerIdAndItemId(Long buyerId, Long sellerId, Long itemId);

    // بررسی وجود مکالمه (برای امتیازدهی بعد از چت)
    /**
     * Performs the "exists by buyer id and seller id and item id" operation.
     *
     * @param buyerId id of the buyer
     * @param sellerId id of the seller
     * @param itemId id of the ad (item)
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     */
    boolean existsByBuyerIdAndSellerIdAndItemId(Long buyerId, Long sellerId, Long itemId);
}