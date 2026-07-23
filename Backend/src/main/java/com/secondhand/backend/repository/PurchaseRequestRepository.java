package com.secondhand.backend.repository;

import com.secondhand.backend.constant.PurchaseRequestStatus;
import com.secondhand.backend.entity.PurchaseRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Spring Data JPA repository for {@code PurchaseRequest} entities.
 * <p>
 * This interface performs read and write operations on the database via Spring Data JPA; method implementations are generated at runtime from the method name or the {@code @Query} annotation.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
@Repository
public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, Long> {

    /**
     * Finds by item id order by created at desc.
     *
     * @param itemId id of the ad (item)
     * @return a {@code List<PurchaseRequest>} with the results; empty if nothing matches
     */
    List<PurchaseRequest> findByItemIdOrderByCreatedAtDesc(Long itemId);

    /**
     * Finds by buyer id order by created at desc.
     *
     * @param buyerId id of the buyer
     * @return a {@code List<PurchaseRequest>} with the results; empty if nothing matches
     */
    List<PurchaseRequest> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);

    // تمام درخواست‌های ثبت‌شده روی آگهی‌های یک فروشنده (item.user.id)
    /**
     * Finds by item user id order by created at desc.
     *
     * @param sellerId id of the seller
     * @return a {@code List<PurchaseRequest>} with the results; empty if nothing matches
     */
    List<PurchaseRequest> findByItemUserIdOrderByCreatedAtDesc(Long sellerId);

    /**
     * Finds by item id and status.
     *
     * @param itemId id of the ad (item)
     * @param status the status value
     * @return a {@code List<PurchaseRequest>} with the results; empty if nothing matches
     */
    List<PurchaseRequest> findByItemIdAndStatus(Long itemId, PurchaseRequestStatus status);

    /**
     * Performs the "exists by item id and buyer id and status" operation.
     *
     * @param itemId id of the ad (item)
     * @param buyerId id of the buyer
     * @param status the status value
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     */
    boolean existsByItemIdAndBuyerIdAndStatus(Long itemId, Long buyerId, PurchaseRequestStatus status);
}
