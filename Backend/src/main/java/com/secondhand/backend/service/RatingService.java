package com.secondhand.backend.service;

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
    public RatingRepository ratingRepository;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public ItemRepository itemRepository;

    public Rating addRating(Long itemId, Long raterId, int score, String comment) {

        if (score < 1 || score > 5) {
            throw new RuntimeException("امتیاز وارد شده باید عددی بین ۱ تا ۵ باشد!");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("آگهی یافت نشد"));

        User rater = userRepository.findById(raterId)
                .orElseThrow(() -> new RuntimeException("کاربر ثبت‌کننده امتیاز یافت نشد"));

        User seller = item.getUser();

        if (seller.getId().equals(raterId)) {
            throw new RuntimeException("شما نمی‌توانید به خودتان امتیاز بدهید!");
        }

        Optional<Rating> existingRating = ratingRepository.findByRaterIdAndItemId(raterId, itemId);
        if (existingRating.isPresent()) {
            throw new RuntimeException("شما قبلاً برای این آگهی امتیاز ثبت کرده‌اید!");
        }

        Rating rating = new Rating();
        rating.score = score;
        rating.comment = comment;
        rating.item = item;
        rating.rater = rater;
        rating.seller = seller;

        return ratingRepository.save(rating);
    }

    public double getSellerAverageRating(Long sellerId) {
        List<Rating> ratings = ratingRepository.findBySellerId(sellerId);
        if (ratings.isEmpty()) {
            return 0.0;
        }

        double sum = 0;
        for (Rating r : ratings) {
            sum += r.score;
        }
        return sum / ratings.size();
    }
}
