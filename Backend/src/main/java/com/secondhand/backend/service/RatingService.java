package com.secondhand.backend.service;

import com.secondhand.backend.dto.RatingCreateRequest;
import com.secondhand.backend.dto.RatingResponse;
import com.secondhand.backend.entity.Item;
import com.secondhand.backend.entity.Rating;
import com.secondhand.backend.entity.User;
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

    public RatingResponse addRating(RatingCreateRequest request) {
        if (request.getScore() < 1 || request.getScore() > 5) {
            throw new RuntimeException("امتیاز وارد شده باید عددی بین ۱ تا ۵ باشد!");
        }

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new RuntimeException("آگهی یافت نشد"));

        User rater = userRepository.findById(request.getRaterId())
                .orElseThrow(() -> new RuntimeException("کاربر ثبت‌کننده امتیاز یافت نشد"));

        User seller = item.getUser();

        if (seller.getId().equals(request.getRaterId())) {
            throw new RuntimeException("شما نمی‌توانید به خودتان امتیاز بدهید!");
        }

        Optional<Rating> existingRating = ratingRepository.findByRaterIdAndItemId(request.getRaterId(), request.getItemId());
        if (existingRating.isPresent()) {
            throw new RuntimeException("شما قبلاً برای این آگهی امتیاز ثبت کرده‌اید!");
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
}