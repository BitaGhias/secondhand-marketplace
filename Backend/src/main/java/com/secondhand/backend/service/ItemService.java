package com.secondhand.backend.service;

import com.secondhand.backend.entity.City;
import com.secondhand.backend.entity.Item;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.repository.CityRepository;
import com.secondhand.backend.repository.ItemRepository;
import com.secondhand.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    public CityRepository cityRepository;

    public Item createItem(Item item, Long userId, Long cityId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("کاربر یافت نشد"));

        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new RuntimeException("شهر مورد نظر یافت نشد"));
        item.city = city;

        //اگر کاربر وجود داشت، او را به عنوان «صاحب آگهی» معرفی می‌کنیم
        item.setUser(user);

        return itemRepository.save(item);
    }

    public List<Item> getApprovedItems() {
        return itemRepository.findByStatus("APPROVED");
    }

    public Item updateItemStatus(Long itemId, String newStatus) {

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("آگهی مورد نظر یافت نشد"));

        String statusUpper = newStatus.toUpperCase();

        if (!statusUpper.equals("APPROVED") && !statusUpper.equals("REJECTED") && !statusUpper.equals("PENDING")) {
            throw new RuntimeException("وضعیت ارسال شده معتبر نیست. باید APPROVED، REJECTED یا PENDING باشد.");
        }

        item.setStatus(statusUpper);
        return itemRepository.save(item);
    }

    public List<Item> getPendingItems() {
        return itemRepository.findByStatus("PENDING");
    }

    public List<Item> getApprovedItemsByCategory(Long categoryId) {
        return itemRepository.findByCategoryIdAndStatus(categoryId, "APPROVED");
    }

    public List<Item> getItemByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("کاربر یافت نشد");
        }
        return itemRepository.findByUserId(userId);
    }

    public void deleteItem(Long itemId, Long userId) {

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("آگهی مورد نظر یافت نشد"));

        if (!item.getUser().getId().equals(userId) && !userId.equals(1L)) {
            throw new RuntimeException("شما اجازه حذف این آگهی را ندارید!");
        }

        itemRepository.delete(item);
    }

    public List<Item> searchItems(String keyword) {
        return itemRepository.findByStatusAndTitleContainingIgnoreCaseOrStatusAndDescriptionContainingIgnoreCase(
                "APPROVED", keyword, "APPROVED", keyword
        );
    }

    public List<Item> getItemsByCity(Long cityId) {
        return itemRepository.findByStatusAndCityId("APPROVED", cityId);

    }

    public Item markAsSold(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("آگهی یافت نشد"));

        if (!item.getUser().getId().equals(userId)) {
            throw new RuntimeException("شما مالک این آگهی نیستید و اجازه تغییر وضعیت آن را ندارید!");
        }

        item.setStatus("SOLD");
        return itemRepository.save(item);
    }
}
