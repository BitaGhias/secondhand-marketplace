package com.secondhand.backend.service;

import com.secondhand.backend.constant.ItemStatus;
import com.secondhand.backend.dto.rating.RatingCreateRequest;
import com.secondhand.backend.dto.rating.RatingResponse;
import com.secondhand.backend.entity.Conversation;
import com.secondhand.backend.entity.Item;
import com.secondhand.backend.entity.Rating;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.exception.custom.BadRequestException;
import com.secondhand.backend.exception.custom.ResourceNotFoundException;
import com.secondhand.backend.repository.ConversationRepository;
import com.secondhand.backend.repository.ItemRepository;
import com.secondhand.backend.repository.RatingRepository;
import com.secondhand.backend.repository.UserRepository;
import com.secondhand.backend.util.UserValidationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class RatingService {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    private RatingResponse convertToResponse(Rating rating) {
        return new RatingResponse(
                rating.getId(),
                rating.getScore(),
                rating.getComment(),
                rating.getItem() != null ? rating.getItem().getId() : null,
                rating.getItem() != null ? rating.getItem().getTitle() : "آگهی حذف شده",
                rating.getRater() != null ? rating.getRater().getId() : null,
                rating.getRater() != null ? rating.getRater().getUsername() : "کاربر ناشناس",
                rating.getSeller() != null ? rating.getSeller().getId() : null,
                rating.getSeller() != null ? rating.getSeller().getUsername() : "کاربر ناشناس"
        );
    }

    public RatingResponse addRating(RatingCreateRequest request, Long raterId) {
        if (request.getScore() < 1 || request.getScore() > 5)
            throw new BadRequestException("امتیاز وارد شده باید عددی بین ۱ تا ۵ باشد!");

        User rater = userRepository.findById(raterId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر ثبت‌کننده امتیاز یافت نشد"));
        UserValidationHelper.validateActiveAndNotBlocked(rater);

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("آگهی یافت نشد"));

        if (item.getStatus() != ItemStatus.APPROVED && item.getStatus() != ItemStatus.SOLD)
            throw new BadRequestException("این آگهی قابل امتیازدهی نیست!");

        User seller = item.getUser();
        if (seller.getId().equals(raterId))
            throw new BadRequestException("شما نمی‌توانید به خودتان امتیاز بدهید!");

        // بررسی اینکه آیا کاربر خرید کرده یا چت داشته
        boolean isBuyer = item.getStatus() == ItemStatus.SOLD
                && item.getBuyer() != null
                && item.getBuyer().getId().equals(raterId);

        boolean hasChatted = false;
        if (!isBuyer) {
            hasChatted = conversationRepository.existsByBuyerIdAndSellerIdAndItemId(raterId, seller.getId(), item.getId());
        }

        if (!isBuyer && !hasChatted) {
            throw new BadRequestException("امتیازدهی تنها پس از خرید کالا یا انجام چت با فروشنده امکان‌پذیر است!");
        }

        Optional<Rating> existingRating = ratingRepository.findByRaterIdAndItemId(raterId, request.getItemId());
        if (existingRating.isPresent())
            throw new BadRequestException("شما قبلاً برای این آگهی امتیاز ثبت کرده‌اید!");

        Rating rating = new Rating();
        rating.setScore(request.getScore());
        rating.setComment(request.getComment());
        rating.setItem(item);
        rating.setRater(rater);
        rating.setSeller(seller);

        return convertToResponse(ratingRepository.save(rating));
    }

    public double getSellerAverageRating(Long sellerId) {
        if (!userRepository.existsById(sellerId))
            throw new ResourceNotFoundException("فروشنده یافت نشد");
        return ratingRepository.averageScoreBySellerId(sellerId);
    }

    public long getSellerRatingCount(Long sellerId) {
        if (!userRepository.existsById(sellerId))
            throw new ResourceNotFoundException("فروشنده یافت نشد");
        return ratingRepository.countBySellerId(sellerId);
    }

    public List<RatingResponse> getSellerRatings(Long sellerId) {
        if (!userRepository.existsById(sellerId))
            throw new ResourceNotFoundException("فروشنده یافت نشد");
        return ratingRepository.findBySellerId(sellerId).stream()
                .map(this::convertToResponse)
                .toList();
    }

    public boolean hasUserRatedItem(Long raterId, Long itemId) {
        return ratingRepository.findByRaterIdAndItemId(raterId, itemId).isPresent();
    }
}