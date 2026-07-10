package com.secondhand.backend.service;

import com.secondhand.backend.constant.ItemStatus;
import com.secondhand.backend.constant.Role;
import com.secondhand.backend.dto.ImageResponse;
import com.secondhand.backend.dto.ItemCreateRequest;
import com.secondhand.backend.dto.ItemResponse;
import com.secondhand.backend.dto.ItemUpdateRequest;
import com.secondhand.backend.entity.*;
import com.secondhand.backend.exception.custom.BadRequestException;
import com.secondhand.backend.exception.custom.ForbiddenException;
import com.secondhand.backend.exception.custom.ResourceNotFoundException;
import com.secondhand.backend.repository.CategoryRepository;
import com.secondhand.backend.repository.CityRepository;
import com.secondhand.backend.repository.ImageRepository;
import com.secondhand.backend.repository.ItemRepository;
import com.secondhand.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ImageRepository imageRepository;

    private ItemResponse convertToResponse(Item item) {
        List<Image> images = imageRepository.findByItemId(item.getId());

        List<ImageResponse> imageResponses = new ArrayList<>();
        for (Image img : images) {
            imageResponses.add(new ImageResponse(img.getId(), img.getImagePath()));
        }

        // دریافت نام دسته‌بندی و والد آن
        String categoryName = item.getCategory() != null ? item.getCategory().getName() : "بدون دسته‌بندی";
        String parentCategoryName = "";
        if (item.getCategory() != null && item.getCategory().getParent() != null) {
            parentCategoryName = item.getCategory().getParent().getName();
        }

        return new ItemResponse(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getPrice(),
                item.getStatus().name(),
                categoryName,
                item.getCity() != null ? item.getCity().getName() : "بدون شهر",
                item.getUser() != null ? item.getUser().getUsername() : "کاربر ناشناس",
                item.getUser() != null ? item.getUser().getId() : null,
                imageResponses
        );
    }

    private List<ItemResponse> convertToResponseList(List<Item> items) {
        List<ItemResponse> responses = new ArrayList<>();
        for (Item item : items) {
            responses.add(convertToResponse(item));
        }
        return responses;
    }

    public ItemResponse addItem(ItemCreateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر یافت نشد"));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("دسته‌بندی یافت نشد"));
        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("شهر یافت نشد"));

        Item item = new Item();
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setStatus(ItemStatus.PENDING);
        item.setUser(user);
        item.setCategory(category);
        item.setCity(city);

        Item savedItem = itemRepository.save(item);

        List<MultipartFile> images = request.getImages();
        if (images != null && !images.isEmpty()) {
            try {
                String uploadDir = "uploads/";
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                for (MultipartFile file : images) {
                    if (!file.isEmpty()) {
                        String originalFileName = file.getOriginalFilename();
                        String extension = "";
                        if (originalFileName != null && originalFileName.contains(".")) {
                            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
                        }

                        String fileName = System.currentTimeMillis() + extension;
                        Path filePath = uploadPath.resolve(fileName);

                        Files.write(filePath, file.getBytes());

                        Image image = new Image();
                        image.setImagePath(filePath.toString());
                        image.setItem(savedItem);
                        imageRepository.save(image);
                    }
                }
            } catch (IOException e) {
                throw new BadRequestException("خطا در ذخیره تصویر: " + e.getMessage());
            }
        }

        return convertToResponse(savedItem);
    }

    public List<ItemResponse> getApprovedItems() {
        List<Item> items = itemRepository.findByStatus(ItemStatus.APPROVED.name());
        return convertToResponseList(items);
    }

    public ItemResponse updateItemStatus(Long requesterAdminId, Long itemId, String newStatus) {
        User requester = userRepository.findById(requesterAdminId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر درخواست‌کننده یافت نشد"));

        if (requester.getRole() != Role.ADMIN) {
            throw new ForbiddenException("شما دسترسی ادمین به این عملیات را ندارید!");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("آگهی مورد نظر یافت نشد"));

        ItemStatus status;
        try {
            status = ItemStatus.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("وضعیت ارسال شده معتبر نیست. باید PENDING، APPROVED، REJECTED یا SOLD باشد.");
        }

        item.setStatus(status);
        Item updatedItem = itemRepository.save(item);
        return convertToResponse(updatedItem);
    }

    public List<ItemResponse> getPendingItems(Long requesterAdminId) {
        User requester = userRepository.findById(requesterAdminId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر درخواست‌کننده یافت نشد"));

        if (requester.getRole() != Role.ADMIN) {
            throw new ForbiddenException("شما دسترسی ادمین به این عملیات را ندارید!");
        }

        List<Item> items = itemRepository.findByStatus(ItemStatus.PENDING.name());
        return convertToResponseList(items);
    }

    public List<ItemResponse> getApprovedItemsByCategory(Long categoryId) {
        List<Item> items = itemRepository.findByCategoryIdAndStatus(categoryId, ItemStatus.APPROVED.name());
        return convertToResponseList(items);
    }

    public List<ItemResponse> getItemByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("کاربر یافت نشد");
        }
        // آگهی‌های کاربر رو بگیر به جز DELETED
        List<Item> items = itemRepository.findByUserIdAndStatusNot(userId, ItemStatus.DELETED.name());
        return convertToResponseList(items);
    }

    public void deleteItem(Long itemId, Long userId) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر درخواست‌کننده یافت نشد"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("آگهی مورد نظر یافت نشد"));

        if (!item.getUser().getId().equals(userId) && requester.getRole() != Role.ADMIN) {
            throw new ForbiddenException("شما اجازه حذف این آگهی را ندارید!");
        }

        item.setStatus(ItemStatus.DELETED);
        itemRepository.save(item);
    }

    public List<ItemResponse> searchItems(String keyword) {
        List<Item> items = itemRepository.findByStatusAndTitleContainingIgnoreCaseOrStatusAndDescriptionContainingIgnoreCase(
                ItemStatus.APPROVED.name(), keyword, ItemStatus.APPROVED.name(), keyword
        );
        return convertToResponseList(items);
    }

    public List<ItemResponse> getItemsByCity(Long cityId) {
        List<Item> items = itemRepository.findByStatusAndCityId(ItemStatus.APPROVED.name(), cityId);
        return convertToResponseList(items);
    }

    public ItemResponse markAsSold(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("آگهی یافت نشد"));

        if (!item.getUser().getId().equals(userId)) {
            throw new ForbiddenException("شما مالک این آگهی نیستید و اجازه تغییر وضعیت آن را ندارید!");
        }

        item.setStatus(ItemStatus.SOLD);
        Item updatedItem = itemRepository.save(item);
        return convertToResponse(updatedItem);
    }

    public ItemResponse getItemById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("آگهی مورد نظر یافت نشد"));

        if (item.getStatus() != ItemStatus.APPROVED) {
            throw new BadRequestException("این آگهی قابل نمایش نیست");
        }
        return convertToResponse(item);
    }

    public ItemResponse updateItem(Long itemId, Long userId, ItemUpdateRequest request) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("آگهی مورد نظر یافت نشد"));

        if (!item.getUser().getId().equals(userId)) {
            throw new ForbiddenException("شما اجازه ویرایش این آگهی را ندارید!");
        }

        if (item.getStatus() == ItemStatus.SOLD) {
            throw new BadRequestException("آگهی فروخته شده قابل ویرایش نیست!");
        }
        if (item.getStatus() == ItemStatus.REJECTED) {
            throw new BadRequestException("آگهی رد شده قابل ویرایش نیست!");
        }

        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            item.setTitle(request.getTitle());
        }

        if (request.getDescription() != null && !request.getDescription().trim().isEmpty()) {
            item.setDescription(request.getDescription());
        }

        if (request.getPrice() != null && request.getPrice() > 0) {
            item.setPrice(request.getPrice());
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("دسته‌بندی یافت نشد"));
            item.setCategory(category);
        }

        if (request.getCityId() != null) {
            City city = cityRepository.findById(request.getCityId())
                    .orElseThrow(() -> new ResourceNotFoundException("شهر یافت نشد"));
            item.setCity(city);
        }

        Item updatedItem = itemRepository.save(item);
        return convertToResponse(updatedItem);
    }
}