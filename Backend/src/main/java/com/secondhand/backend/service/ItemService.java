package com.secondhand.backend.service;

import com.secondhand.backend.constant.ItemStatus;
import com.secondhand.backend.constant.Role;
import com.secondhand.backend.dto.item.*;
import com.secondhand.backend.entity.*;
import com.secondhand.backend.exception.custom.BadRequestException;
import com.secondhand.backend.exception.custom.ForbiddenException;
import com.secondhand.backend.exception.custom.ResourceNotFoundException;
import com.secondhand.backend.repository.*;
import com.secondhand.backend.util.UserValidationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ItemService {

    private static final Logger logger = LoggerFactory.getLogger(ItemService.class);

    @Autowired private ItemRepository itemRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CityRepository cityRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ImageRepository imageRepository;

    private void validateItemPrice(Long price) {
        if (price == null) throw new BadRequestException("قیمت آگهی الزامی است!");
        if (price <= 0) throw new BadRequestException("قیمت باید بزرگتر از ۰ باشد!");
    }

    private void validateItemTitleAndDescription(String title, String description) {
        if (title == null || title.trim().isEmpty())
            throw new BadRequestException("عنوان آگهی نمی‌تواند خالی باشد!");
        if (description == null || description.trim().isEmpty())
            throw new BadRequestException("توضیحات آگهی نمی‌تواند خالی باشد!");
    }

    private void validateUserIsAdmin(User user) {
        if (user.getRole() != Role.ADMIN)
            throw new ForbiddenException("شما دسترسی ادمین ندارید!");
    }

    private void validateUserIsAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر یافت نشد"));
        validateUserIsAdmin(user);
    }

    private void validateUserIsOwnerOrAdmin(Item item, Long userId, User requester) {
        boolean isOwner = item.getUser().getId().equals(userId);
        boolean isAdmin = requester.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin)
            throw new ForbiddenException("شما اجازه این عملیات را ندارید!");
    }

    private void validateItemNotSoldOrDeleted(Item item) {
        if (item.getStatus() == ItemStatus.SOLD || item.getStatus() == ItemStatus.DELETED)
            throw new BadRequestException("این آگهی قابل تغییر نیست!");
    }

    private void validateImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) return;
        if (images.size() > 5) throw new BadRequestException("حداکثر ۵ تصویر مجاز است!");

        for (MultipartFile file : images) {
            if (!file.isEmpty()) {
                if (file.getContentType() == null || !file.getContentType().startsWith("image/"))
                    throw new BadRequestException("فایل ارسال شده تصویر نیست!");

                String originalFileName = file.getOriginalFilename();
                if (originalFileName != null) {
                    String extension = "";
                    if (originalFileName.contains("."))
                        extension = originalFileName.substring(originalFileName.lastIndexOf(".")).toLowerCase();

                    List<String> allowedExtensions = List.of(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp");
                    if (!allowedExtensions.contains(extension))
                        throw new BadRequestException("فرمت فایل تصویر مجاز نیست!");
                }
                if (file.getSize() > 5 * 1024 * 1024)
                    throw new BadRequestException("حجم تصویر نباید بیشتر از ۵ مگابایت باشد!");
            }
        }
    }

    private ItemResponse convertToResponse(Item item) {
        List<Image> images = imageRepository.findByItemId(item.getId());
        List<ImageResponse> imageResponses = new ArrayList<>();
        for (Image img : images)
            imageResponses.add(new ImageResponse(img.getId(), img.getImagePath()));

        String categoryName = item.getCategory() != null ? item.getCategory().getName() : "بدون دسته‌بندی";
        String parentCategoryName = "";
        if (item.getCategory() != null && item.getCategory().getParent() != null)
            parentCategoryName = item.getCategory().getParent().getName();

        ItemResponse response = new ItemResponse(
                item.getId(), item.getTitle(), item.getDescription(), item.getPrice(),
                item.getStatus().name(), categoryName, parentCategoryName,
                item.getCity() != null ? item.getCity().getName() : "بدون شهر",
                item.getUser() != null ? item.getUser().getUsername() : "کاربر ناشناس",
                item.getUser() != null ? item.getUser().getId() : null,
                imageResponses, item.getRejectionReason()
        );

        if (item.getBuyer() != null) {
            response.setBuyerId(item.getBuyer().getId());
            response.setBuyerUsername(item.getBuyer().getUsername());
        }
        return response;
    }

    private List<ItemResponse> convertToResponseList(List<Item> items) {
        List<ItemResponse> responses = new ArrayList<>();
        for (Item item : items) responses.add(convertToResponse(item));
        return responses;
    }

    // FIX: @Transactional اضافه شد - ذخیره آگهی و تصویر اتمیک است
    @Transactional
    public ItemResponse addItem(ItemCreateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر یافت نشد"));
        UserValidationHelper.validateActiveAndNotBlocked(user);

        validateItemTitleAndDescription(request.getTitle(), request.getDescription());
        validateItemPrice(request.getPrice());
        validateImages(request.getImages());

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
                if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

                for (MultipartFile file : images) {
                    if (!file.isEmpty()) {
                        String originalFileName = file.getOriginalFilename();
                        String extension = "";
                        if (originalFileName != null && originalFileName.contains("."))
                            extension = originalFileName.substring(originalFileName.lastIndexOf("."));

                        String fileName = System.currentTimeMillis() + extension;
                        Path filePath = uploadPath.resolve(fileName);
                        Files.write(filePath, file.getBytes());

                        Image image = new Image();
                        image.setImagePath(filePath.toString().replace("\\", "/"));
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
        return convertToResponseList(itemRepository.findByStatus(ItemStatus.APPROVED));
    }

    public ItemResponse updateItemStatus(Long requesterAdminId, Long itemId, String newStatus, String rejectionReason) {
        validateUserIsAdmin(requesterAdminId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("آگهی مورد نظر یافت نشد"));

        if (item.getUser().getId().equals(requesterAdminId))
            throw new BadRequestException("شما نمی‌توانید آگهی خودتان را تایید یا رد کنید!");

        validateItemNotSoldOrDeleted(item);

        ItemStatus status;
        try {
            status = ItemStatus.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("وضعیت ارسال شده معتبر نیست.");
        }

        if (status != ItemStatus.APPROVED && status != ItemStatus.REJECTED)
            throw new BadRequestException("ادمین فقط می‌تواند آگهی را تایید یا رد کند!");

        if (item.getStatus() == status)
            throw new BadRequestException("آگهی در حال حاضر در وضعیت " + status.name() + " قرار دارد!");

        if (status == ItemStatus.REJECTED) {
            if (rejectionReason == null || rejectionReason.trim().isEmpty())
                throw new BadRequestException("لطفاً دلیل رد کردن آگهی را وارد کنید!");
            item.setRejectionReason(rejectionReason);
        } else {
            item.setRejectionReason(null);
        }

        item.setStatus(status);
        return convertToResponse(itemRepository.save(item));
    }

    public List<ItemResponse> getPendingItems(Long requesterAdminId) {
        validateUserIsAdmin(requesterAdminId);
        return convertToResponseList(itemRepository.findByStatus(ItemStatus.PENDING));
    }

    public List<ItemResponse> getApprovedItemsByCategory(Long categoryId) {
        return convertToResponseList(itemRepository.findByCategoryIdAndStatus(categoryId, ItemStatus.APPROVED));
    }

    public List<ItemResponse> getItemByUser(Long userId) {
        if (!userRepository.existsById(userId))
            throw new ResourceNotFoundException("کاربر یافت نشد");
        return convertToResponseList(itemRepository.findByUserIdAndStatusNot(userId, ItemStatus.DELETED));
    }

    public void deleteItem(Long itemId, Long userId) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر درخواست‌کننده یافت نشد"));
        UserValidationHelper.validateActiveAndNotBlocked(requester);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("آگهی مورد نظر یافت نشد"));

        validateUserIsOwnerOrAdmin(item, userId, requester);
        validateItemNotSoldOrDeleted(item);

        List<Image> images = imageRepository.findByItemId(itemId);
        for (Image image : images) {
            try {
                Path filePath = Paths.get(image.getImagePath());
                if (Files.exists(filePath)) Files.delete(filePath);
            } catch (IOException e) {
                logger.warn("خطا در حذف فایل تصویر: {} - {}", image.getImagePath(), e.getMessage());
            }
        }
        imageRepository.deleteAll(images);
        item.setStatus(ItemStatus.DELETED);
        itemRepository.save(item);
    }

    public List<ItemResponse> searchItems(String keyword) {
        if (keyword == null || keyword.trim().isEmpty())
            throw new BadRequestException("کلمه کلیدی جستجو نمی‌تواند خالی باشد!");
        return convertToResponseList(
                itemRepository.findByStatusAndTitleContainingIgnoreCaseOrStatusAndDescriptionContainingIgnoreCase(
                        ItemStatus.APPROVED, keyword
                )
        );
    }

    public List<ItemResponse> getItemsByCity(Long cityId) {
        if (!cityRepository.existsById(cityId))
            throw new ResourceNotFoundException("شهر یافت نشد");
        return convertToResponseList(itemRepository.findByStatusAndCityId(ItemStatus.APPROVED, cityId));
    }

    public ItemResponse markAsSold(Long itemId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر یافت نشد"));
        UserValidationHelper.validateActiveAndNotBlocked(user);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("آگهی یافت نشد"));

        if (!item.getUser().getId().equals(userId))
            throw new ForbiddenException("شما مالک این آگهی نیستید!");
        if (item.getStatus() == ItemStatus.SOLD)
            throw new BadRequestException("آگهی قبلاً فروخته شده است!");
        if (item.getStatus() == ItemStatus.DELETED)
            throw new BadRequestException("آگهی حذف شده است!");

        item.setStatus(ItemStatus.SOLD);
        return convertToResponse(itemRepository.save(item));
    }

    public ItemResponse getItemById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("آگهی مورد نظر یافت نشد"));
        if (item.getStatus() != ItemStatus.APPROVED && item.getStatus() != ItemStatus.SOLD)
            throw new BadRequestException("این آگهی قابل نمایش نیست");
        return convertToResponse(item);
    }

    // FIX: متد جدید - ادمین می‌تواند هر آگهی با هر وضعیتی را ببیند
    public ItemResponse getItemByIdForAdmin(Long adminId, Long itemId) {
        validateUserIsAdmin(adminId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("آگهی مورد نظر یافت نشد"));
        return convertToResponse(item);
    }

    public ItemResponse updateItem(Long itemId, Long userId, ItemUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر یافت نشد"));
        UserValidationHelper.validateActiveAndNotBlocked(user);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("آگهی مورد نظر یافت نشد"));

        if (!item.getUser().getId().equals(userId))
            throw new ForbiddenException("شما اجازه ویرایش این آگهی را ندارید!");

        // FIX: آگهی رد‌شده قابل ویرایش است و به PENDING برمی‌گردد
        if (item.getStatus() == ItemStatus.SOLD || item.getStatus() == ItemStatus.DELETED)
            throw new BadRequestException("این آگهی قابل ویرایش نیست!");

        if (request.getTitle() != null && !request.getTitle().trim().isEmpty())
            item.setTitle(request.getTitle());
        if (request.getDescription() != null && !request.getDescription().trim().isEmpty())
            item.setDescription(request.getDescription());
        if (request.getPrice() != null) {
            if (request.getPrice() <= 0) throw new BadRequestException("قیمت باید بزرگتر از ۰ باشد!");
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

        // FIX: اگر آگهی رد شده بود، بعد از ویرایش به PENDING برمی‌گردد
        if (item.getStatus() == ItemStatus.REJECTED) {
            item.setStatus(ItemStatus.PENDING);
            item.setRejectionReason(null);
        }

        return convertToResponse(itemRepository.save(item));
    }

    public List<ItemResponse> searchItemsAdvanced(ItemSearchRequest request) {
        String sortBy = request.getSortBy();
        if (sortBy == null || sortBy.trim().isEmpty()) sortBy = "newest";

        List<Item> items = itemRepository.searchAdvanced(
                request.getKeyword(), request.getCategoryId(), request.getCityId(),
                request.getMinPrice(), request.getMaxPrice()
        );

        switch (sortBy.toLowerCase()) {
            case "newest" -> items.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
            case "oldest" -> items.sort((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));
            case "price_asc" -> items.sort((a, b) -> Long.compare(a.getPrice(), b.getPrice()));
            case "price_desc" -> items.sort((a, b) -> Long.compare(b.getPrice(), a.getPrice()));
            default -> items.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        }
        return convertToResponseList(items);
    }

    public ItemResponse purchaseItem(Long itemId, Long buyerId) {
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("کاربر یافت نشد"));
        UserValidationHelper.validateActiveAndNotBlocked(buyer);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("آگهی یافت نشد"));

        if (item.getUser().getId().equals(buyerId))
            throw new BadRequestException("شما نمی‌توانید آگهی خودتان را بخرید!");
        if (item.getStatus() == ItemStatus.SOLD)
            throw new BadRequestException("این آگهی قبلاً فروخته شده است!");
        if (item.getStatus() != ItemStatus.APPROVED)
            throw new BadRequestException("این آگهی قابل خرید نیست!");

        item.setBuyer(buyer);
        item.setStatus(ItemStatus.SOLD);
        return convertToResponse(itemRepository.save(item));
    }

    public List<ItemResponse> getPurchasedItems(Long buyerId) {
        if (!userRepository.existsById(buyerId))
            throw new ResourceNotFoundException("کاربر یافت نشد");
        return convertToResponseList(itemRepository.findByBuyerId(buyerId));
    }

    public List<ItemResponse> getItemsByUserForAdmin(Long requesterAdminId, Long userId) {
        validateUserIsAdmin(requesterAdminId);
        if (!userRepository.existsById(userId))
            throw new ResourceNotFoundException("کاربر یافت نشد");
        return convertToResponseList(itemRepository.findByUserId(userId));
    }

    public List<ImageResponse> getItemImages(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("آگهی یافت نشد"));
        if (item.getStatus() != ItemStatus.APPROVED && item.getStatus() != ItemStatus.SOLD)
            throw new BadRequestException("این آگهی قابل نمایش نیست");
        return imageRepository.findByItemId(itemId).stream()
                .map(img -> new ImageResponse(img.getId(), img.getImagePath()))
                .toList();
    }
}