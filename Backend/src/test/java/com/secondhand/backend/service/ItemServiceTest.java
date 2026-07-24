package com.secondhand.backend.service;

import com.secondhand.backend.constant.ItemStatus;
import com.secondhand.backend.constant.PurchaseRequestStatus;
import com.secondhand.backend.dto.item.ItemCreateRequest;
import com.secondhand.backend.dto.item.ItemResponse;
import com.secondhand.backend.dto.item.ItemSearchRequest;
import com.secondhand.backend.entity.Item;
import com.secondhand.backend.entity.PurchaseRequest;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.exception.custom.BadRequestException;
import com.secondhand.backend.repository.CategoryRepository;
import com.secondhand.backend.repository.CityRepository;
import com.secondhand.backend.repository.ImageRepository;
import com.secondhand.backend.repository.ItemRepository;
import com.secondhand.backend.repository.PurchaseRequestRepository;
import com.secondhand.backend.repository.RatingRepository;
import com.secondhand.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ItemService}: advanced search sorting,
 * ad-price validation and the direct-purchase flow.
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock private RatingRepository ratingRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private UserRepository userRepository;
    @Mock private CityRepository cityRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ImageRepository imageRepository;
    @Mock private PurchaseRequestRepository purchaseRequestRepository;

    @InjectMocks
    private ItemService itemService;

    // ---------- helpers ----------

    private User user(long id) {
        User u = new User();
        u.setId(id);
        u.setUsername("user" + id);
        u.setActive(true);
        u.setBlocked(false);
        return u;
    }

    private Item item(long id, long price, LocalDateTime createdAt, User seller) {
        Item i = new Item();
        i.setId(id);
        i.setTitle("کالا " + id);
        i.setDescription("توضیحات");
        i.setPrice(price);
        i.setStatus(ItemStatus.APPROVED);
        i.setCreatedAt(createdAt);
        i.setUser(seller);
        return i;
    }

    // ---------- searchItemsAdvanced ----------

    /** Without an explicit sort, results must be ordered newest-first. */
    @Test
    void searchItemsAdvanced_defaultSort_returnsNewestFirst() {
        User seller = user(1L);
        Item older = item(1L, 100_000L, LocalDateTime.now().minusDays(2), seller);
        Item newer = item(2L, 200_000L, LocalDateTime.now(), seller);
        when(itemRepository.searchAdvanced(any(), any(), any(), any(), any()))
                .thenReturn(new ArrayList<>(List.of(older, newer)));

        List<ItemResponse> result = itemService.searchItemsAdvanced(new ItemSearchRequest());

        assertEquals(2L, result.get(0).getId());
        assertEquals(1L, result.get(1).getId());
    }

    /** sortBy=price_asc must order results by ascending price. */
    @Test
    void searchItemsAdvanced_priceAscending_sortsByPrice() {
        User seller = user(1L);
        Item expensive = item(1L, 900_000L, LocalDateTime.now(), seller);
        Item cheap = item(2L, 50_000L, LocalDateTime.now(), seller);
        when(itemRepository.searchAdvanced(any(), any(), any(), any(), any()))
                .thenReturn(new ArrayList<>(List.of(expensive, cheap)));

        ItemSearchRequest request = new ItemSearchRequest();
        request.setSortBy("price_asc");
        List<ItemResponse> result = itemService.searchItemsAdvanced(request);

        assertEquals(50_000L, result.get(0).getPrice());
        assertEquals(900_000L, result.get(1).getPrice());
    }

    /** sortBy=rating_desc must put ads of the best-rated seller first. */
    @Test
    void searchItemsAdvanced_ratingDescending_sortsBySellerRating() {
        User topSeller = user(1L);
        User weakSeller = user(2L);
        Item weakSellerAd = item(1L, 100_000L, LocalDateTime.now(), weakSeller);
        Item topSellerAd = item(2L, 100_000L, LocalDateTime.now(), topSeller);
        when(itemRepository.searchAdvanced(any(), any(), any(), any(), any()))
                .thenReturn(new ArrayList<>(List.of(weakSellerAd, topSellerAd)));
        when(ratingRepository.averageScoreGroupedBySeller())
                .thenReturn(List.of(new Object[]{1L, 4.8}, new Object[]{2L, 1.5}));

        ItemSearchRequest request = new ItemSearchRequest();
        request.setSortBy("rating_desc");
        List<ItemResponse> result = itemService.searchItemsAdvanced(request);

        assertEquals(2L, result.get(0).getId());
        assertEquals(1L, result.get(1).getId());
    }

    // ---------- addItem validation ----------

    /** A negative price must be rejected with 400 before anything is saved. */
    @Test
    void addItem_negativePrice_throwsBadRequest() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(user(5L)));
        ItemCreateRequest request = new ItemCreateRequest();
        request.setTitle("گوشی موبایل");
        request.setDescription("در حد نو");
        request.setPrice(-100L);

        assertThrows(BadRequestException.class, () -> itemService.addItem(request, 5L));

        verify(itemRepository, never()).save(any(Item.class));
    }

    /** A blank title must be rejected with 400 before anything is saved. */
    @Test
    void addItem_blankTitle_throwsBadRequest() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(user(5L)));
        ItemCreateRequest request = new ItemCreateRequest();
        request.setTitle("   ");
        request.setDescription("در حد نو");
        request.setPrice(100_000L);

        assertThrows(BadRequestException.class, () -> itemService.addItem(request, 5L));

        verify(itemRepository, never()).save(any(Item.class));
    }

    // ---------- purchaseItem ----------

    /** Buying your own ad must be rejected with 400. */
    @Test
    void purchaseItem_ownAd_throwsBadRequest() {
        User seller = user(1L);
        Item ad = item(10L, 500_000L, LocalDateTime.now(), seller);
        when(userRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(ad));

        assertThrows(BadRequestException.class, () -> itemService.purchaseItem(10L, 1L));

        verify(itemRepository, never()).save(any(Item.class));
    }

    /** An already-sold ad cannot be purchased again. */
    @Test
    void purchaseItem_alreadySold_throwsBadRequest() {
        User seller = user(1L);
        User buyer = user(2L);
        Item ad = item(10L, 500_000L, LocalDateTime.now(), seller);
        ad.setStatus(ItemStatus.SOLD);
        when(userRepository.findById(2L)).thenReturn(Optional.of(buyer));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(ad));

        assertThrows(BadRequestException.class, () -> itemService.purchaseItem(10L, 2L));

        verify(itemRepository, never()).save(any(Item.class));
    }

    /** A successful purchase marks the ad SOLD and declines other pending requests. */
    @Test
    void purchaseItem_success_marksSoldAndDeclinesPendingRequests() {
        User seller = user(1L);
        User buyer = user(2L);
        Item ad = item(10L, 500_000L, LocalDateTime.now(), seller);
        when(userRepository.findById(2L)).thenReturn(Optional.of(buyer));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(itemRepository.save(ad)).thenReturn(ad);
        PurchaseRequest pending = new PurchaseRequest();
        pending.setStatus(PurchaseRequestStatus.PENDING);
        when(purchaseRequestRepository.findByItemIdAndStatus(10L, PurchaseRequestStatus.PENDING))
                .thenReturn(List.of(pending));

        ItemResponse response = itemService.purchaseItem(10L, 2L);

        assertEquals(ItemStatus.SOLD, ad.getStatus());
        assertEquals("SOLD", response.getStatus());
        assertEquals(PurchaseRequestStatus.DECLINED, pending.getStatus());
        verify(purchaseRequestRepository).save(pending);
    }
}
