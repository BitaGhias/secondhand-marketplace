package com.secondhand.backend.service;

import com.secondhand.backend.constant.ItemStatus;
import com.secondhand.backend.constant.PurchaseRequestStatus;
import com.secondhand.backend.dto.purchase.PurchaseRequestResponse;
import com.secondhand.backend.entity.Item;
import com.secondhand.backend.entity.PurchaseRequest;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.exception.custom.BadRequestException;
import com.secondhand.backend.exception.custom.ForbiddenException;
import com.secondhand.backend.exception.custom.ResourceNotFoundException;
import com.secondhand.backend.repository.ItemRepository;
import com.secondhand.backend.repository.PurchaseRequestRepository;
import com.secondhand.backend.repository.UserRepository;
import com.secondhand.backend.util.UserValidationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Business-logic service for "purchase request" operations.
 * <p>
 * This class implements the core business logic and sits between the controller layer and the repository layer. Validation and access control are enforced here and a proper exception is thrown when a rule is violated.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
@Service
public class PurchaseRequestService {

    @Autowired private PurchaseRequestRepository purchaseRequestRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private UserRepository userRepository;

    /**
     * Converts to response.
     *
     * @param pr the "pr" value of type {@code PurchaseRequest}
     * @return the resulting {@code PurchaseRequestResponse} instance
     */
    private PurchaseRequestResponse convertToResponse(PurchaseRequest pr) {
        PurchaseRequestResponse r = new PurchaseRequestResponse();
        r.setId(pr.getId());
        if (pr.getItem() != null) {
            r.setItemId(pr.getItem().getId());
            r.setItemTitle(pr.getItem().getTitle());
            if (pr.getItem().getUser() != null) {
                r.setSellerId(pr.getItem().getUser().getId());
                r.setSellerUsername(pr.getItem().getUser().getUsername());
            }
        }
        if (pr.getBuyer() != null) {
            r.setBuyerId(pr.getBuyer().getId());
            r.setBuyerUsername(pr.getBuyer().getUsername());
            r.setBuyerFullName(pr.getBuyer().getFullName());
            r.setBuyerPhone(pr.getBuyer().getPhoneNumber());
            r.setBuyerEmail(pr.getBuyer().getEmail());
        }
        r.setStatus(pr.getStatus().name());
        r.setCreatedAt(pr.getCreatedAt());
        r.setRespondedAt(pr.getRespondedAt());
        return r;
    }

    /**
     * Converts list.
     *
     * @param list the "list" value of type {@code List<PurchaseRequest>}
     * @return a {@code List<PurchaseRequestResponse>} with the results; empty if nothing matches
     */
    private List<PurchaseRequestResponse> convertList(List<PurchaseRequest> list) {
        List<PurchaseRequestResponse> out = new ArrayList<>();
        for (PurchaseRequest pr : list) out.add(convertToResponse(pr));
        return out;
    }

    /**
     * Creates.
     *
     * @param itemId id of the ad (item)
     * @param buyerId id of the buyer
     * @return the resulting {@code PurchaseRequestResponse} instance
     */
    public PurchaseRequestResponse create(Long itemId, Long buyerId) {
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر یافت نشد"));
        UserValidationHelper.validateActiveAndNotBlocked(buyer);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("آگهی یافت نشد"));

        if (item.getUser().getId().equals(buyerId))
            throw new BadRequestException("شما نمی‌توانید برای آگهی خودتان درخواست خرید ثبت کنید!");
        if (item.getStatus() == ItemStatus.SOLD)
            throw new BadRequestException("این آگهی قبلاً فروخته شده است!");
        if (item.getStatus() != ItemStatus.APPROVED)
            throw new BadRequestException("این آگهی قابل خرید نیست!");
        if (purchaseRequestRepository.existsByItemIdAndBuyerIdAndStatus(itemId, buyerId, PurchaseRequestStatus.PENDING))
            throw new BadRequestException("شما قبلاً برای این آگهی درخواست خرید ثبت کرده‌اید!");

        PurchaseRequest pr = new PurchaseRequest();
        pr.setItem(item);
        pr.setBuyer(buyer);
        pr.setStatus(PurchaseRequestStatus.PENDING);
        pr.setCreatedAt(LocalDateTime.now());
        return convertToResponse(purchaseRequestRepository.save(pr));
    }

    /**
     * Lists for item.
     *
     * @param itemId id of the ad (item)
     * @param requesterId the "requester id" value of type {@code Long}
     * @return a {@code List<PurchaseRequestResponse>} with the results; empty if nothing matches
     */
    public List<PurchaseRequestResponse> listForItem(Long itemId, Long requesterId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("آگهی یافت نشد"));
        if (!item.getUser().getId().equals(requesterId))
            throw new ForbiddenException("فقط صاحب آگهی می‌تواند درخواست‌های خرید را ببیند!");
        return convertList(purchaseRequestRepository.findByItemIdOrderByCreatedAtDesc(itemId));
    }

    /**
     * Performs the "incoming" operation.
     *
     * @param sellerId id of the seller
     * @return a {@code List<PurchaseRequestResponse>} with the results; empty if nothing matches
     */
    public List<PurchaseRequestResponse> incoming(Long sellerId) {
        if (!userRepository.existsById(sellerId))
            throw new ResourceNotFoundException("کاربر یافت نشد");
        return convertList(purchaseRequestRepository.findByItemUserIdOrderByCreatedAtDesc(sellerId));
    }

    /**
     * Performs the "mine" operation.
     *
     * @param buyerId id of the buyer
     * @return a {@code List<PurchaseRequestResponse>} with the results; empty if nothing matches
     */
    public List<PurchaseRequestResponse> mine(Long buyerId) {
        if (!userRepository.existsById(buyerId))
            throw new ResourceNotFoundException("کاربر یافت نشد");
        return convertList(purchaseRequestRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId));
    }

    /**
     * Performs the "accept" operation.
     *
     * @param requestId id of the request
     * @param sellerId id of the seller
     * @return the resulting {@code PurchaseRequestResponse} instance
     */
    @Transactional
    public PurchaseRequestResponse accept(Long requestId, Long sellerId) {
        PurchaseRequest pr = purchaseRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("درخواست خرید یافت نشد"));
        Item item = pr.getItem();
        if (!item.getUser().getId().equals(sellerId))
            throw new ForbiddenException("فقط صاحب آگهی می‌تواند درخواست را تایید کند!");
        if (pr.getStatus() != PurchaseRequestStatus.PENDING)
            throw new BadRequestException("این درخواست قبلاً پاسخ داده شده است!");
        if (item.getStatus() == ItemStatus.SOLD)
            throw new BadRequestException("این آگهی قبلاً فروخته شده است!");

        // ثبت فروش
        item.setBuyer(pr.getBuyer());
        item.setStatus(ItemStatus.SOLD);
        itemRepository.save(item);

        pr.setStatus(PurchaseRequestStatus.ACCEPTED);
        pr.setRespondedAt(LocalDateTime.now());
        PurchaseRequest saved = purchaseRequestRepository.save(pr);

        // رد خودکار بقیه درخواست‌های در انتظار
        for (PurchaseRequest other : purchaseRequestRepository.findByItemIdAndStatus(item.getId(), PurchaseRequestStatus.PENDING)) {
            other.setStatus(PurchaseRequestStatus.DECLINED);
            other.setRespondedAt(LocalDateTime.now());
            purchaseRequestRepository.save(other);
        }

        return convertToResponse(saved);
    }

    /**
     * Performs the "decline" operation.
     *
     * @param requestId id of the request
     * @param sellerId id of the seller
     * @return the resulting {@code PurchaseRequestResponse} instance
     */
    @Transactional
    public PurchaseRequestResponse decline(Long requestId, Long sellerId) {
        PurchaseRequest pr = purchaseRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("درخواست خرید یافت نشد"));
        if (!pr.getItem().getUser().getId().equals(sellerId))
            throw new ForbiddenException("فقط صاحب آگهی می‌تواند درخواست را رد کند!");
        if (pr.getStatus() != PurchaseRequestStatus.PENDING)
            throw new BadRequestException("این درخواست قبلاً پاسخ داده شده است!");
        pr.setStatus(PurchaseRequestStatus.DECLINED);
        pr.setRespondedAt(LocalDateTime.now());
        return convertToResponse(purchaseRequestRepository.save(pr));
    }
}
