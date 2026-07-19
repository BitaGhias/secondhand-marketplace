package com.secondhand.backend.service;

import com.secondhand.backend.constant.ItemStatus;
import com.secondhand.backend.dto.RatingCreateRequest;
import com.secondhand.backend.dto.RatingResponse;
import com.secondhand.backend.entity.Conversation;
import com.secondhand.backend.entity.Item;
import com.secondhand.backend.entity.Rating;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.exception.custom.BadRequestException;
import com.secondhand.backend.exception.custom.ForbiddenException;
import com.secondhand.backend.exception.custom.ResourceNotFoundException;
import com.secondhand.backend.repository.ConversationRepository;
import com.secondhand.backend.repository.ItemRepository;
import com.secondhand.backend.repository.RatingRepository;
import com.secondhand.backend.repository.UserRepository;
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

    private void validateUser(User user) {
        if (!user.isActive()) {
            throw new ForbiddenException("حساب کاربری شما فعال نیست!");
        }
        if (user.isBlocked()) {
            throw new ForbiddenException("حساب کاربری شما مسدود شده است!");
        }
    }

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

        if (request.getScore() < 1 || request.getScore() > 5) {
            throw new BadRequestException("امتیاز وارد شده باید عددی بین ۱ تا ۵ باشد!");
        }

        User rater = userRepository.findById(raterId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر ثبت‌کننده امتیاز یافت نشد"));
        validateUser(rater);

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("آگهی یافت نشد"));

        //  بررسی اینکه آگهی تایید شده باشه (فقط APPROVED یا SOLD قابل امتیازدهی هستن)
        if (item.getStatus() != ItemStatus.APPROVED && item.getStatus() != ItemStatus.SOLD) {
            throw new BadRequestException("این آگهی قابل امتیازدهی نیست!");
        }

        User seller = item.getUser();
        if (seller.getId().equals(raterId)) {
            throw new BadRequestException("شما نمی‌توانید به خودتان امتیاز بدهید!");
        }

        //  فقط خریدار واقعی آگهی (پس از خرید) می‌تواند امتیاز دهد
        boolean isBuyer = item.getStatus() == ItemStatus.SOLD
                && item.getBuyer() != null
                && item.getBuyer().getId().equals(raterId);

        if (!isBuyer) {
            throw new BadRequestException("امتیازدهی فقط پس از خرید این کالا امکان‌پذیر است!");
        }


        Optional<Rating> existingRating = ratingRepository.findByRaterIdAndItemId(raterId, request.getItemId());
        if (existingRating.isPresent()) {
            throw new BadRequestException("شما قبلاً برای این آگهی امتیاز ثبت کرده‌اید!");
        }

        Rating rating = new Rating();
        rating.setScore(request.getScore());
        rating.setComment(request.getComment());
        rating.setItem(item);
        rating.setRater(rater);
        rating.setSeller(seller);

        Rating savedRating = ratingRepository.save(rating);
        return convertToResponse(savedRating);
    }

    public double getSellerAverageRating(Long sellerId) {

        if (!userRepository.existsById(sellerId)) {
            throw new ResourceNotFoundException("فروشنده یافت نشد");
        }

        List<Rating> ratings = ratingRepository.findBySellerId(sellerId);
        if (ratings.isEmpty()) {
            return 0.0;
        }

        double sum = 0;
        for (Rating r : ratings) {
            sum += r.getScore();
        }
        return sum / ratings.size();
    }

    public long getSellerRatingCount(Long sellerId) {
        if (!userRepository.existsById(sellerId)) {
            throw new ResourceNotFoundException("فروشنده یافت نشد");
        }
        return ratingRepository.findBySellerId(sellerId).size();
    }

    public List<RatingResponse> getSellerRatings(Long sellerId) {
        if (!userRepository.existsById(sellerId)) {
            throw new ResourceNotFoundException("فروشنده یافت نشد");
        }

        List<Rating> ratings = ratingRepository.findBySellerId(sellerId);
        return ratings.stream()
                .map(this::convertToResponse)
                .toList();
    }
}