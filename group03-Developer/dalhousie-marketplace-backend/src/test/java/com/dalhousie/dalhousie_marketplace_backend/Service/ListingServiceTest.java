package com.dalhousie.dalhousie_marketplace_backend.Service;

import com.dalhousie.dalhousie_marketplace_backend.DTO.ListingDTO;
import com.dalhousie.dalhousie_marketplace_backend.model.*;
import com.dalhousie.dalhousie_marketplace_backend.repository.CategoryRepository;
import com.dalhousie.dalhousie_marketplace_backend.repository.ListingImageRepository;
import com.dalhousie.dalhousie_marketplace_backend.repository.ListingRepository;
import com.dalhousie.dalhousie_marketplace_backend.repository.UserRepository;
import com.dalhousie.dalhousie_marketplace_backend.service.ListingImageService;
import com.dalhousie.dalhousie_marketplace_backend.service.ListingService;
import com.dalhousie.dalhousie_marketplace_backend.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the ListingService class
 */
public class ListingServiceTest {

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ListingImageRepository listingImageRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ListingService listingService;
    @Mock
    private MultipartFile imageFile;

    @Mock
    private ListingImageService listingImageService;

    private Listing activeListing1;
    private Listing activeListing2;
    private Listing inactiveListing;
    private Listing soldListing;
    private Category category;
    // Constants to eliminate magic numbers
    private static final long SELLER_ID = 1L;
    private static final long UNVERIFIED_SELLER_ID = 2L;
    private static final long LISTING_ID_1 = 1L;
    private static final long LISTING_ID_2 = 2L;
    private static final long LISTING_ID_3 = 3L;
    private static final long LISTING_ID_4 = 4L;
    private static final long NON_EXISTENT_ID = 99L;
    private static final long INVALID_LISTING_ID = 6L;

    private static final long CATEGORY_ID = 1L;
    private static final long INVALID_CATEGORY_ID = 99L;

    private static final double PRICE_100 = 100.0;
    private static final double PRICE_150 = 150.0;
    private static final double PRICE_200 = 200.0;
    private static final double PRICE_300 = 300.0;
    private static final double RATING_4_5 = 4.5;

    private static final int QUANTITY = 1;
    private static final int EXPECTED_VALUE = 2;
    private static final int EXPECTED_VALUE_3 = 3;
    private static final int REVIEW_COUNT_10 = 10;
    private static final long IMAGE_SIZE_3L = 3L;

    /**
     * Setup method that runs before each test
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create a mock seller
        User seller = new User();
        seller.setUserId(SELLER_ID);
        seller.setEmail("seller@example.com");
        seller.setIsVerified(true);

        // Create sample listings with different statuses
        activeListing1 = createListing(LISTING_ID_1, "Active Listing 1", PRICE_100, Listing.ListingStatus.ACTIVE);
        activeListing2 = createListing(LISTING_ID_2, "Active Listing 2", PRICE_200, Listing.ListingStatus.ACTIVE);
        inactiveListing = createListing(LISTING_ID_3, "Inactive Listing", PRICE_150, Listing.ListingStatus.INACTIVE);
        soldListing = createListing(LISTING_ID_4, "Sold Listing", PRICE_300, Listing.ListingStatus.SOLD);

        category = new Category();
        category.setId(CATEGORY_ID);
        category.setName("Electronics");
    }

    /**
     * Helper method to create a listing with specified properties
     */
    private Listing createListing(Long id, String title, Double price, Listing.ListingStatus status) {
        Listing listing = new Listing();
        listing.setId(id);
        listing.setTitle(title);
        listing.setDescription("Description for " + title);
        listing.setPrice(price);
        listing.setQuantity(QUANTITY);
        listing.setCategoryId(CATEGORY_ID);
        listing.setStatus(status);
        listing.setViews(0);
        listing.setCreatedAt(new Date());
        listing.setUpdatedAt(new Date());
        try {
            listing.setBiddingAllowed(false);
        } catch (NoSuchMethodError | NoSuchFieldError e) {
            System.err.println("Field 'biddingAllowed' not found in Listing: " + e.getMessage());
        }
        return listing;
    }

    /**
     * Test that getActiveListings returns only listings with ACTIVE status
     */
    @Test
    void getActiveListings_shouldReturnOnlyActiveListings() {
        // Arrange
        List<Listing> allListings = Arrays.asList(activeListing1, activeListing2, inactiveListing, soldListing);
        List<Listing> activeListings = Arrays.asList(activeListing1, activeListing2);

        when(listingRepository.findByStatus(Listing.ListingStatus.ACTIVE)).thenReturn(activeListings);

        // Act
        List<Listing> result = listingService.getActiveListings();

        // Assert
        assertEquals(EXPECTED_VALUE, result.size());

        // Verify only active listings are returned
        for (Listing listing : result) {
            assertEquals(Listing.ListingStatus.ACTIVE, listing.getStatus());
        }

        // Verify the repository method was called with the correct status
        verify(listingRepository).findByStatus(Listing.ListingStatus.ACTIVE);

        // Verify the titles are what we expect
        List<String> expectedTitles = Arrays.asList("Active Listing 1", "Active Listing 2");
        List<String> actualTitles = new ArrayList<>();
        for (Listing listing : result) {
            actualTitles.add(listing.getTitle());
        }
        assertTrue(actualTitles.containsAll(expectedTitles));
    }

    @Test
    void getAllListings_ReturnsAllListings() {
        List<Listing> allListings = Arrays.asList(activeListing1, inactiveListing, soldListing);
        when(listingRepository.findAll()).thenReturn(allListings);

        List<Listing> result = listingService.getAllListings();

        assertEquals(EXPECTED_VALUE_3, result.size());
        verify(listingRepository).findAll();
    }

    @Test
    void searchListings_WithKeyword_ReturnsMatches() {
        List<Listing> matches = Arrays.asList(activeListing1);
        when(listingRepository.searchByKeyword("Active")).thenReturn(matches);

        List<Listing> result = listingService.searchListings("Active");

        assertEquals(1, result.size());
        assertEquals("Active Listing 1", result.get(0).getTitle());
        verify(listingRepository).searchByKeyword("Active");
    }

    @Test
    void searchListings_WithEmptyKeyword_ReturnsActiveListings() {
        List<Listing> activeListings = Arrays.asList(activeListing1, activeListing2);
        when(listingRepository.findByStatus(Listing.ListingStatus.ACTIVE)).thenReturn(activeListings);

        List<Listing> result = listingService.searchListings("");

        assertEquals(EXPECTED_VALUE, result.size());
        verify(listingRepository).findByStatus(Listing.ListingStatus.ACTIVE);
    }

    @Test
    void createListing_Success() throws IOException {
        User seller = new User();
        seller.setUserId(SELLER_ID);
        seller.setEmail("seller@example.com");
        seller.setusername("SellerUser");
        seller.setIsVerified(true);

        activeListing1.setSeller(seller);
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(userRepository.findByEmail(seller.getEmail())).thenReturn(Optional.of(seller));
        when(listingRepository.save(any(Listing.class))).thenReturn(activeListing1);
        when(userRepository.findAll()).thenReturn(Arrays.asList(seller, new User()));
        when(imageFile.isEmpty()).thenReturn(false);
        when(imageFile.getBytes()).thenReturn(new byte[]{1, 2, 3});
        when(imageFile.getContentType()).thenReturn("image/jpeg");
        when(imageFile.getSize()).thenReturn(IMAGE_SIZE_3L);
        when(listingImageService.saveImage(eq(activeListing1.getId()), any(MultipartFile.class), eq(seller.getUserId()), anyBoolean()))
                .thenReturn(new ListingImage());

        Listing result = listingService.createListing(activeListing1, seller.getEmail(), new MultipartFile[]{imageFile});

        assertEquals(activeListing1.getId(), result.getId());
        assertEquals(activeListing1.getTitle(), result.getTitle());
        verify(listingRepository).save(any(Listing.class));
        verify(notificationService, atLeastOnce()).sendNotification(any(User.class), eq(NotificationType.ITEM), anyString());
        verify(listingImageService).saveImage(activeListing1.getId(), imageFile, seller.getUserId(), true);
    }

    @Test
    void createListing_InvalidCategory_ThrowsException() throws IOException {
        User seller = new User();
        seller.setUserId(SELLER_ID);
        seller.setEmail("seller@example.com");
        seller.setusername("SellerUser");
        seller.setIsVerified(true);
        activeListing1.setCategoryId(INVALID_CATEGORY_ID);
        when(categoryRepository.findById(INVALID_CATEGORY_ID)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            listingService.createListing(activeListing1, seller.getEmail(), null);
        });

        assertEquals("Category with ID 99 does not exist.", exception.getMessage());
        verify(listingRepository, never()).save(any());
    }

    @Test
    void createListing_UnverifiedSeller_ThrowsException() throws IOException {
        User unverifiedSeller = new User(); // Local initialization
        unverifiedSeller.setUserId(UNVERIFIED_SELLER_ID);
        unverifiedSeller.setEmail("unverified@example.com");
        unverifiedSeller.setusername("UnverifiedUser");
        unverifiedSeller.setIsVerified(false); // This line should now work

        Listing listing = createListing(INVALID_LISTING_ID, "Test Listing", 100.0, Listing.ListingStatus.ACTIVE);
        listing.setSeller(unverifiedSeller);

        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(userRepository.findByEmail(unverifiedSeller.getEmail())).thenReturn(Optional.of(unverifiedSeller));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            listingService.createListing(listing, unverifiedSeller.getEmail(), null);
        });

        assertEquals("User is not verified. Please verify your email before posting.", exception.getMessage());
        verify(listingRepository, never()).save(any());
    }

    @Test
    void getListingById_Success() {

        when(listingRepository.findById(activeListing1.getId())).thenReturn(Optional.of(activeListing1));
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(listingRepository.save(any(Listing.class))).thenReturn(activeListing1);

        ListingDTO result = listingService.getListingById(activeListing1.getId());
        assertNotNull(result);
        verify(listingRepository).save(any(Listing.class)); // Views incremented
    }

    @Test
    void getListingById_NotFound_ThrowsException() {
        when(listingRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            listingService.getListingById(NON_EXISTENT_ID);
        });

        assertEquals("Listing not found", exception.getMessage());
        verify(listingRepository, never()).save(any());
    }

    @Test
    void updateListingRatingStats_Success() {
        when(listingRepository.findById(activeListing1.getId())).thenReturn(Optional.of(activeListing1));
        when(listingRepository.save(any(Listing.class))).thenReturn(activeListing1);

        listingService.updateListingRatingStats(activeListing1.getId(), RATING_4_5, REVIEW_COUNT_10);

        verify(listingRepository).save(argThat(l -> l.getAverageRating() == RATING_4_5 && l.getReviewCount() == REVIEW_COUNT_10));
    }

    @Test
    void updateListingRatingStats_ListingNotFound_ThrowsException() {
        when(listingRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            listingService.updateListingRatingStats(NON_EXISTENT_ID, RATING_4_5, REVIEW_COUNT_10);
        });

        assertEquals("Listing not found with id: 99", exception.getMessage());
        verify(listingRepository, never()).save(any());
    }

    @Test
    void getBiddableListings_ReturnsBiddableActiveListings() {
        activeListing1.setBiddingAllowed(true);
        List<Listing> biddableListings = Arrays.asList(activeListing1);
        when(listingRepository.findByBiddingAllowedAndStatus(true, Listing.ListingStatus.ACTIVE)).thenReturn(biddableListings);

        List<Listing> result = listingService.getBiddableListings();

        assertEquals(1, result.size());
        assertTrue(result.get(0).getBiddingAllowed());
        verify(listingRepository).findByBiddingAllowedAndStatus(true, Listing.ListingStatus.ACTIVE);
    }

    @Test
    void getBiddingListingsBySeller_ReturnsSellerBiddableListings() {
        User seller = new User();
        seller.setUserId(SELLER_ID);
        seller.setEmail("seller@example.com");
        seller.setusername("SellerUser");
        seller.setIsVerified(true);

        activeListing1.setBiddingAllowed(true);
        List<Listing> sellerListings = Arrays.asList(activeListing1, inactiveListing);
        when(listingRepository.findBySellerId(seller.getUserId())).thenReturn(sellerListings);

        List<Listing> result = listingService.getBiddingListingsBySeller(seller.getUserId());

        assertEquals(1, result.size());
        assertTrue(result.get(0).getBiddingAllowed());
        verify(listingRepository).findBySellerId(seller.getUserId());
    }
}