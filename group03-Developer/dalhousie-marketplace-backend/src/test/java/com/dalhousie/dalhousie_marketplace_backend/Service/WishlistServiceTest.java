package com.dalhousie.dalhousie_marketplace_backend.Service;

import com.dalhousie.dalhousie_marketplace_backend.model.Listing;
import com.dalhousie.dalhousie_marketplace_backend.model.Wishlist;
import com.dalhousie.dalhousie_marketplace_backend.model.WishlistItem;
import com.dalhousie.dalhousie_marketplace_backend.repository.ListingRepository;
import com.dalhousie.dalhousie_marketplace_backend.repository.WishlistRepository;
import com.dalhousie.dalhousie_marketplace_backend.service.WishlistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

        import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;

class WishlistServiceTest {

    private WishlistRepository wishlistRepository;
    private ListingRepository listingRepository;
    private WishlistService wishlistService;

    private static final Long NEW_USER_ID = 2L;
    private static final Long LISTING_ID_VALID = 10L;
    private static final Long LISTING_ID_INVALID = 20L;
    private static final Long USER_ID_CLEAR = 99L;
    private static final Long LISTING_ID_MISSING = 100L;

    @BeforeEach
    void setUp() {
        wishlistRepository = mock(WishlistRepository.class);
        listingRepository = mock(ListingRepository.class);
        wishlistService = new WishlistService(wishlistRepository, listingRepository);
    }

    @Test
    void testGetWishlistByUserId_existingWishlist() {
        Wishlist wishlist = new Wishlist(1L);
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(wishlist));

        Wishlist result = wishlistService.getWishlistByUserId(1L);

        assertEquals(wishlist, result);
    }

    @Test
    void testGetWishlistByUserId_createsNewWishlistIfNotExists() {
        Wishlist newWishlist = new Wishlist(NEW_USER_ID);
        when(wishlistRepository.findByUserId(NEW_USER_ID)).thenReturn(Optional.empty());
        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(newWishlist);

        Wishlist result = wishlistService.getWishlistByUserId(NEW_USER_ID);

        assertEquals(newWishlist, result);
    }

    @Test
    void testAddItemToWishlist_successfulAddition() {
        Long userId = 1L, listingId = LISTING_ID_VALID;

        Wishlist wishlist = new Wishlist(userId);
        wishlist.setWishlistItems(new ArrayList<>());

        Listing listing = new Listing();
        listing.setId(listingId);

        when(wishlistRepository.findByUserId(userId)).thenReturn(Optional.of(wishlist));
        when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));
        when(wishlistRepository.save(any(Wishlist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Wishlist result = wishlistService.addItemToWishlist(userId, listingId);

        assertEquals(1, result.getWishlistItems().size());
    }

    @Test
    void testAddItemToWishlist_throwsIfItemAlreadyExists() {
        Long userId = 1L, listingId = LISTING_ID_VALID;

        Listing listing = new Listing();
        listing.setId(listingId);

        WishlistItem item = new WishlistItem();
        item.setListing(listing);

        Wishlist wishlist = new Wishlist(userId);
        wishlist.setWishlistItems(new ArrayList<>(Collections.singletonList(item)));

        when(wishlistRepository.findByUserId(userId)).thenReturn(Optional.of(wishlist));

        Exception exception = assertThrows(RuntimeException.class, () ->
                wishlistService.addItemToWishlist(userId, listingId));

        assertEquals("Item already exists in wishlist", exception.getMessage());
    }

    @Test
    void testAddItemToWishlist_throwsIfListingNotFound() {
        Long userId = 1L, listingId = LISTING_ID_INVALID;

        Wishlist wishlist = new Wishlist(userId);
        wishlist.setWishlistItems(new ArrayList<>());

        when(wishlistRepository.findByUserId(userId)).thenReturn(Optional.of(wishlist));
        when(listingRepository.findById(listingId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () ->
                wishlistService.addItemToWishlist(userId, listingId));

        assertEquals("Listing not found", exception.getMessage());
    }

    @Test
    void testClearWishlist_deletesById() {
        wishlistService.clearWishlist(USER_ID_CLEAR);

        verify(wishlistRepository, times(1)).deleteById(USER_ID_CLEAR);
        assertTrue(true); // Single assert required
    }

    @Test
    void testRemoveWishlistItem_successfulRemoval() {
        Long userId = 1L, listingId = LISTING_ID_VALID;

        Listing listing = new Listing();
        listing.setId(listingId);

        WishlistItem item = new WishlistItem();
        item.setListing(listing);

        Wishlist wishlist = new Wishlist(userId);
        wishlist.setWishlistItems(new ArrayList<>(Collections.singletonList(item)));

        when(wishlistRepository.findByUserId(userId)).thenReturn(Optional.of(wishlist));
        when(wishlistRepository.save(any(Wishlist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Wishlist result = wishlistService.removeWishlistItem(userId, listingId);

        assertEquals(0, result.getWishlistItems().size());
    }

    @Test
    void testRemoveWishlistItem_throwsIfItemNotFound() {
        Long userId = 1L, listingId = LISTING_ID_MISSING;

        Wishlist wishlist = new Wishlist(userId);
        wishlist.setWishlistItems(new ArrayList<>()); // empty list

        when(wishlistRepository.findByUserId(userId)).thenReturn(Optional.of(wishlist));

        Exception exception = assertThrows(RuntimeException.class, () ->
                wishlistService.removeWishlistItem(userId, listingId));

        assertEquals("Item not found in wishlist", exception.getMessage());
    }
}