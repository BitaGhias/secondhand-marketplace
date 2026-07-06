package com.secondhand.backend.repository;

import com.secondhand.backend.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findBySellerId(Long sellerId);
    Optional<Rating> findByRaterIdAndItemId(Long raterId, Long itemId);
}