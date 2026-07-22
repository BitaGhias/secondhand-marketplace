package com.secondhand.backend.repository;

import com.secondhand.backend.constant.PurchaseRequestStatus;
import com.secondhand.backend.entity.PurchaseRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, Long> {

    List<PurchaseRequest> findByItemIdOrderByCreatedAtDesc(Long itemId);

    List<PurchaseRequest> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);

    // تمام درخواست‌های ثبت‌شده روی آگهی‌های یک فروشنده (item.user.id)
    List<PurchaseRequest> findByItemUserIdOrderByCreatedAtDesc(Long sellerId);

    List<PurchaseRequest> findByItemIdAndStatus(Long itemId, PurchaseRequestStatus status);

    boolean existsByItemIdAndBuyerIdAndStatus(Long itemId, Long buyerId, PurchaseRequestStatus status);
}
