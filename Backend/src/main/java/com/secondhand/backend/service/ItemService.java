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


    private void validateUserIsActiveAndNotBlocked(User user) {
        if (!user.isActive()) {
            throw new ForbiddenException("حساب کاربری شما فعال نیست!");
        }
        if (user.isBlocked()) {
            throw new ForbiddenException("حساب کاربری شما مسدود شده است!");
        }
    }

    private void validateItemPrice(double price) {
        if (price <= 0) {
            throw new BadRequestException("قیمت باید بزرگتر از ۰ باشد!");
        }
    }

    private void validateItemTitleAndDescription(String title, String description) {
        if (title == null || title.trim().isEmpty()) {
            throw new BadRequestException("عنوان آگهی نمی‌تواند خالی باشد!");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new BadRequestException("توضیحات آگهی نمی‌تواند خالی باشد!");
        }
    }

    private void validateUserIsAdmin(User user) {
        if (user.getRole() != Role.ADMIN) {
            throw new ForbiddenException("شما دسترسی ادمین به این عملیات را ندارید!");
        }
    }

    private void validateUserIsOwnerOrAdmin(Item item, Long userId, User requester) {
        if (!item.getUser().getId().equals(userId) && requester.getRole() != Role.ADMIN) {
            throw new ForbiddenException("شما اجازه این عملیات را ندارید!");
        }
    }

    private void validateItemNotSoldOrDeleted(Item item) {
        if (item.getStatus() == ItemStatus.SOLD) {
            throw new BadRequestException("آگهی فروخته شده قابل تغییر نیست!");
        }
        if (item.getStatus() == ItemStatus.DELETED) {
            throw new BadRequestException("آگهی حذف شده قابل تغییر نیست!");
        }
    }

    private ItemResponse convertToResponse(Item item) {
        List<Image> images = imageRepository.findByItemId(item.getId());
        List<ImageResponse> imageResponses = new ArrayList<>();
        for (Image img : images) {
            imageResponses.add(new ImageResponse(img.getId(), img.getImagePath()));
        }

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
                parentCategoryName,
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
        validateUserIsActiveAndNotBlocked(user);

        validateItemTitleAndDescription(request.getTitle(), request.getDescription());

        validateItemPrice(request.getPrice());

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

        validateUserIsAdmin(requester);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("آگهی مورد نظر یافت نشد"));

        if (item.getUser().getId().equals(requesterAdminId)) {
            throw new BadRequestException("شما نمی‌توانید آگهی خودتان را تایید یا رد کنید!");
        }

        validateItemNotSoldOrDeleted(item);

        if (item.getStatus() == ItemStatus.APPROVED) {
            throw new BadRequestException("آگهی قبلاً تایید شده است!");
        }
        if (item.getStatus() == ItemStatus.REJECTED) {
            throw new BadRequestException("آگهی قبلاً رد شده است!");
        }

        ItemStatus status;
        try {
            status = ItemStatus.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("وضعیت ارسال شده معتبر نیست. باید APPROVED یا REJECTED باشد.");
        }

        // ادمین فقط می‌تونه APPROVED یا REJECTED کنه
        if (status != ItemStatus.APPROVED && status != ItemStatus.REJECTED) {
            throw new BadRequestException("ادمین فقط می‌تواند آگهی را تایید (APPROVED) یا رد (REJECTED) کند!");
        }

        if (item.getStatus() == status) {
            throw new BadRequestException("آگهی در حال حاضر در وضعیت " + status.name() + " قرار دارد!");
        }

        item.setStatus(status);
        Item updatedItem = itemRepository.save(item);
        return convertToResponse(updatedItem);
    }


    public List<ItemResponse> getPendingItems(Long requesterAdminId) {
        User requester = userRepository.findById(requesterAdminId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر درخواست‌کننده یافت نشد"));

        validateUserIsAdmin(requester);

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
        List<Item> items = itemRepository.findByUserIdAndStatusNot(userId, ItemStatus.DELETED.name());
        return convertToResponseList(items);
    }


    public void deleteItem(Long itemId, Long userId) {

        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر درخواست‌کننده یافت نشد"));

        validateUserIsActiveAndNotBlocked(requester);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("آگهی مورد نظر یافت نشد"));

        validateUserIsOwnerOrAdmin(item, userId, requester);
        validateItemNotSoldOrDeleted(item);

        List<Image> images = imageRepository.findByItemId(itemId);
        for (Image image : images) {
            try {
                Path filePath = Paths.get(image.getImagePath());
                if (Files.exists(filePath)) {
                    Files.delete(filePath);  // حذف فایل از روی دیسک
                    System.out.println("🗑️ تصویر حذف شد: " + image.getImagePath());
                }
            } catch (IOException e) {
                System.out.println("⚠️ خطا در حذف تصویر: " + image.getImagePath() + " - " + e.getMessage());
            }
        }

        imageRepository.deleteAll(images);

        item.setStatus(ItemStatus.DELETED);
        itemRepository.save(item);
    }

    public List<ItemResponse> searchItems(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new BadRequestException("کلمه کلیدی جستجو نمی‌تواند خالی باشد!");
        }

        List<Item> items = itemRepository.findByStatusAndTitleContainingIgnoreCaseOrStatusAndDescriptionContainingIgnoreCase(
                ItemStatus.APPROVED.name(), keyword, ItemStatus.APPROVED.name(), keyword
        );
        return convertToResponseList(items);
    }


    public List<ItemResponse> getItemsByCity(Long cityId) {
        if (!cityRepository.existsById(cityId)) {
            throw new ResourceNotFoundException("شهر یافت نشد");
        }
        List<Item> items = itemRepository.findByStatusAndCityId(ItemStatus.APPROVED.name(), cityId);
        return convertToResponseList(items);
    }


    public ItemResponse markAsSold(Long itemId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر یافت نشد"));

        validateUserIsActiveAndNotBlocked(user);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("آگهی یافت نشد"));

        if (!item.getUser().getId().equals(userId)) {
            throw new ForbiddenException("شما مالک این آگهی نیستید!");
        }

        if (item.getStatus() == ItemStatus.SOLD) {
            throw new BadRequestException("آگهی قبلاً فروخته شده است!");
        }
        if (item.getStatus() == ItemStatus.DELETED) {
            throw new BadRequestException("آگهی حذف شده است!");
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر یافت نشد"));

        validateUserIsActiveAndNotBlocked(user);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("آگهی مورد نظر یافت نشد"));

        if (!item.getUser().getId().equals(userId)) {
            throw new ForbiddenException("شما اجازه ویرایش این آگهی را ندارید!");
        }

        validateItemNotSoldOrDeleted(item);

        if (item.getStatus() == ItemStatus.REJECTED) {
            throw new BadRequestException("آگهی رد شده قابل ویرایش نیست!");
        }

        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            item.setTitle(request.getTitle());
        }

        if (request.getDescription() != null && !request.getDescription().trim().isEmpty()) {
            item.setDescription(request.getDescription());
        }

        if (request.getPrice() != null) {
            if (request.getPrice() <= 0) {
                throw new BadRequestException("قیمت باید بزرگتر از ۰ باشد!");
            }
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