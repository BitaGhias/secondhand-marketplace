package com.secondhand.backend.service;

import com.secondhand.backend.constant.Role;
import com.secondhand.backend.dto.ItemCreateRequest;
import com.secondhand.backend.dto.ItemResponse;
import com.secondhand.backend.entity.*;
import com.secondhand.backend.repository.CategoryRepository;
import com.secondhand.backend.repository.CityRepository;
import com.secondhand.backend.repository.ItemRepository;
import com.secondhand.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    public CityRepository cityRepository;

    @Autowired
    public CategoryRepository categoryRepository;

    private ItemResponse convertToResponse(Item item) {
        return new ItemResponse(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getPrice(),
                item.getStatus(),
                item.category != null ? item.category.getName() : "بدون دسته‌بندی",
                item.city != null ? item.city.name : "بدون شهر",
                item.getUser() != null ? item.getUser().getUsername() : "کاربر ناشناس",
                item.getUser() != null ? item.getUser().getId() : null
        );
    }

    private List<ItemResponse> convertToResponseList(List<Item> items) {
        List<ItemResponse> responses = new ArrayList<>();
        for (Item item : items) {
            responses.add(convertToResponse(item));
        }
        return responses;
    }

    public ItemResponse addItem(ItemCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("کاربر یافت نشد"));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("دسته‌بندی یافت نشد"));
        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new RuntimeException("شهر یافت نشد"));

        Item item = new Item();
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setStatus("PENDING");
        item.setUser(user);
        item.category = category;
        item.city = city;

        Item savedItem = itemRepository.save(item);
        return convertToResponse(savedItem);
    }

    public List<ItemResponse> getApprovedItems() {
        List<Item> items = itemRepository.findByStatus("APPROVED");
        return convertToResponseList(items);
    }

    public ItemResponse updateItemStatus(Long requesterAdminId, Long itemId, String newStatus) {
        User requester = userRepository.findById(requesterAdminId)
                .orElseThrow(() -> new RuntimeException("کاربر درخواست‌کننده یافت نشد"));

        if (requester.getRole() != Role.ADMIN) {
            throw new RuntimeException("شما دسترسی ادمین به این عملیات را ندارید!");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("آگهی مورد نظر یافت نشد"));

        String statusUpper = newStatus.toUpperCase();
        if (!statusUpper.equals("APPROVED") && !statusUpper.equals("REJECTED") && !statusUpper.equals("PENDING")) {
            throw new RuntimeException("وضعیت ارسال شده معتبر نیست. باید APPROVED، REJECTED یا PENDING باشد.");
        }

        item.setStatus(statusUpper);
        Item updatedItem = itemRepository.save(item);
        return convertToResponse(updatedItem);
    }

    public List<ItemResponse> getPendingItems(Long requesterAdminId) {
        User requester = userRepository.findById(requesterAdminId)
                .orElseThrow(() -> new RuntimeException("کاربر درخواست‌کننده یافت نشد"));

        if (requester.getRole() != Role.ADMIN) {
            throw new RuntimeException("شما دسترسی ادمین به این عملیات را ندارید!");
        }

        List<Item> items = itemRepository.findByStatus("PENDING");
        return convertToResponseList(items);
    }

    public List<ItemResponse> getApprovedItemsByCategory(Long categoryId) {
        List<Item> items = itemRepository.findByCategoryIdAndStatus(categoryId, "APPROVED");
        return convertToResponseList(items);
    }

    public List<ItemResponse> getItemByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("کاربر یافت نشد");
        }
        List<Item> items = itemRepository.findByUserId(userId);
        return convertToResponseList(items);
    }

    public void deleteItem(Long itemId, Long userId) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("کاربر درخواست‌کننده یافت نشد"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("آگهی مورد نظر یافت نشد"));

        if (!item.getUser().getId().equals(userId) && requester.getRole() != Role.ADMIN) {
            throw new RuntimeException("شما اجازه حذف این آگهی را ندارید!");
        }

        itemRepository.delete(item);
    }

    public List<ItemResponse> searchItems(String keyword) {
        List<Item> items = itemRepository.findByStatusAndTitleContainingIgnoreCaseOrStatusAndDescriptionContainingIgnoreCase(
                "APPROVED", keyword, "APPROVED", keyword
        );
        return convertToResponseList(items);
    }

    public List<ItemResponse> getItemsByCity(Long cityId) {
        List<Item> items = itemRepository.findByStatusAndCityId("APPROVED", cityId);
        return convertToResponseList(items);
    }

    public ItemResponse markAsSold(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("آگهی یافت نشد"));

        if (!item.getUser().getId().equals(userId)) {
            throw new RuntimeException("شما مالک این آگهی نیستید و اجازه تغییر وضعیت آن را ندارید!");
        }

        item.setStatus("SOLD");
        Item updatedItem = itemRepository.save(item);
        return convertToResponse(updatedItem);
    }
}