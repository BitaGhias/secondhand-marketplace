package com.secondhand.backend.service;

import com.secondhand.backend.entity.Favorite;
import com.secondhand.backend.entity.Item;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.repository.FavoriteRepository;
import com.secondhand.backend.repository.ItemRepository;
import com.secondhand.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class FavoriteService {

    @Autowired
    public FavoriteRepository favoriteRepository;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public ItemRepository itemRepository;

    public Favorite addFavorite(Long userId, Long itemId) {

        Optional<Favorite> alreadyFavorited = favoriteRepository.findByUserIdAndItemId(userId, itemId);
        if (alreadyFavorited.isPresent()) {
            throw new RuntimeException("این آگهی از قبل در لیست علاقه‌مندی‌های شما وجود دارد");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("کاربر یافت نشد"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("آگهی یافت نشد"));

        Favorite favorite = new Favorite();
        favorite.user = user;
        favorite.item = item;

        return favoriteRepository.save(favorite);
    }

    public void removeFavorite(Long userId, Long itemId) {
        Favorite favorite = favoriteRepository.findByUserIdAndItemId(userId, itemId)
                .orElseThrow(() -> new RuntimeException("این آگهی در لیست علاقه‌مندی‌های شما نیست"));

        favoriteRepository.delete(favorite);
    }

    public List<Favorite> getUserFavorites(Long userId) {
        return favoriteRepository.findByUserId(userId);
    }
}
