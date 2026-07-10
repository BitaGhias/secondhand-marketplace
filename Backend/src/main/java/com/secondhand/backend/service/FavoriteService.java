package com.secondhand.backend.service;

import com.secondhand.backend.dto.FavoriteRequest;
import com.secondhand.backend.dto.FavoriteResponse;
import com.secondhand.backend.entity.Favorite;
import com.secondhand.backend.entity.Item;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.exception.custom.BadRequestException;
import com.secondhand.backend.exception.custom.ResourceNotFoundException;
import com.secondhand.backend.repository.FavoriteRepository;
import com.secondhand.backend.repository.ItemRepository;
import com.secondhand.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private FavoriteResponse convertToResponse(Favorite favorite) {
        return new FavoriteResponse(
                favorite.getId(),
                favorite.getItem() != null ? favorite.getItem().getId() : null,
                favorite.getItem() != null ? favorite.getItem().getTitle() : "آگهی حذف شده",
                favorite.getItem() != null ? favorite.getItem().getPrice() : 0.0,
                favorite.getItem() != null ? favorite.getItem().getStatus().toString() : "UNKNOWN",
                favorite.getUser() != null ? favorite.getUser().getId() : null
        );
    }

    public FavoriteResponse addFavorite(FavoriteRequest request, Long userId) {
        Optional<Favorite> alreadyFavorited = favoriteRepository.findByUserIdAndItemId(userId, request.getItemId());
        if (alreadyFavorited.isPresent()) {
            throw new BadRequestException("این آگهی از قبل در لیست علاقه‌مندی‌های شما وجود دارد");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر یافت نشد"));
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("آگهی یافت نشد"));

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setItem(item);

        Favorite saved = favoriteRepository.save(favorite);
        return convertToResponse(saved);
    }

    public void removeFavorite(FavoriteRequest request, Long userId) {
        Favorite favorite = favoriteRepository.findByUserIdAndItemId(userId, request.getItemId())
                .orElseThrow(() -> new BadRequestException("این آگهی در لیست علاقه‌مندی‌های شما نیست"));

        favoriteRepository.delete(favorite);
    }

    public List<FavoriteResponse> getUserFavorites(Long userId) {
        List<Favorite> favorites = favoriteRepository.findByUserId(userId);
        List<FavoriteResponse> responses = new ArrayList<>();
        for (Favorite f : favorites) {
            responses.add(convertToResponse(f));
        }
        return responses;
    }
}