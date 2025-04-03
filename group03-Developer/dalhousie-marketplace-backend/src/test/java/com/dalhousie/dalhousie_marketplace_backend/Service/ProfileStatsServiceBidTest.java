package com.dalhousie.dalhousie_marketplace_backend.Service;

import com.dalhousie.dalhousie_marketplace_backend.model.*;
import com.dalhousie.dalhousie_marketplace_backend.repository.*;
import com.dalhousie.dalhousie_marketplace_backend.service.ProfileStatsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for bid-related functionality in the ProfileStatsService.
 * Focuses on statistics related to bidding activity for both buyers and sellers.
 */
@ExtendWith(MockitoExtension.class)
public class ProfileStatsServiceBidTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BidRepository bidRepository;

    @InjectMocks
    private ProfileStatsService profileStatsService;

    private final Long BUYER_ID = 1L;
    private final Long SELLER_ID = 2L;
    private List<Bid> buyerBids;
    private List<Bid> sellerReceivedBids;
    private List<Listing> sellerListings;

    // Constants to eliminate magic numbers
    private static final long BID_ID_1 = 1L;
    private static final long BID_ID_2 = 2L;
    private static final long BID_ID_3 = 3L;
    private static final long BID_ID_4 = 4L;
    private static final long BID_ID_5 = 5L;
    private static final long BID_ID_6 = 6L;
    private static final long BID_ID_7 = 7L;
    private static final long BID_ID_10 = 10L;
    private static final long BID_ID_11 = 11L;
    private static final long BID_ID_12 = 12L;
    private static final long BID_ID_13 = 13L;
    private static final long BID_ID_14 = 14L;
    private static final long BID_ID_15 = 15L;
    private static final long BID_ID_20 = 20L;
    private static final long BID_ID_21 = 21L;

    private static final long LISTING_ID_1 = 1L;
    private static final long LISTING_ID_2 = 2L;

    private static final int TOTAL_BUYER_BIDS = 7;
    private static final long ACTIVE_BUYER_BIDS = 3L;
    private static final long ACCEPTED_BUYER_BIDS = 1L;
    private static final long SUCCESSFUL_BUYER_BIDS = 2L;

    private static final int TOTAL_SELLER_BIDS = 6;
    private static final long ACTIVE_SELLER_BIDS = 3L;

    private static final Double BID_PRICE_100 = 100.0;
    private static final BigDecimal BID_PRICE_150 = BigDecimal.valueOf(150.0);
    private static final BigDecimal BID_PRICE_200 = BigDecimal.valueOf(200.0);
    private static final BigDecimal BID_SALES_TOTAL = BID_PRICE_150.add(BID_PRICE_200);


    /**
     * Sets up test data before each test.
     * Creates test bids, listings and orders.
     */
    @BeforeEach
    void setUp() {
        // Create test buyer's bids with different statuses
        buyerBids = new ArrayList<>();
        buyerBids.add(createBid(BID_ID_1, BidStatus.PENDING));
        buyerBids.add(createBid(BID_ID_2, BidStatus.COUNTERED));
        buyerBids.add(createBid(BID_ID_3, BidStatus.ACCEPTED));
        buyerBids.add(createBid(BID_ID_4, BidStatus.REJECTED));
        buyerBids.add(createBid(BID_ID_5, BidStatus.PAID));
        buyerBids.add(createBid(BID_ID_6, BidStatus.PAID));
        buyerBids.add(createBid(BID_ID_7, BidStatus.PENDING));

        // Create seller's listings
        sellerListings = new ArrayList<>();
        sellerListings.add(createListing(LISTING_ID_1));
        sellerListings.add(createListing(LISTING_ID_2));

        // Create bids received by the seller
        sellerReceivedBids = new ArrayList<>();
        sellerReceivedBids.add(createBid(BID_ID_10, BidStatus.PENDING));
        sellerReceivedBids.add(createBid(BID_ID_11, BidStatus.COUNTERED));
        sellerReceivedBids.add(createBid(BID_ID_12, BidStatus.ACCEPTED));
        sellerReceivedBids.add(createBid(BID_ID_13, BidStatus.REJECTED));
        sellerReceivedBids.add(createBid(BID_ID_14, BidStatus.PAID));
        sellerReceivedBids.add(createBid(BID_ID_15, BidStatus.PENDING));
    }

    /**
     * Creates a test bid with the specified ID and status.
     */
    private Bid createBid(Long id, BidStatus status) {
        Bid bid = new Bid();
        bid.setId(id);
        bid.setStatus(status);
        bid.setProposedPrice(BID_PRICE_100);

        // Create minimal objects to satisfy relationships
        User buyer = new User();
        buyer.setUserId(BUYER_ID);

        User seller = new User();
        seller.setUserId(SELLER_ID);

        Listing listing = new Listing();
        listing.setId(id);
        listing.setSeller(seller);

        bid.setBuyer(buyer);
        bid.setListing(listing);

        return bid;
    }

    /**
     * Creates a test listing with the specified ID.
     */
    private Listing createListing(Long id) {
        Listing listing = new Listing();
        listing.setId(id);

        User seller = new User();
        seller.setUserId(SELLER_ID);
        listing.setSeller(seller);

        return listing;
    }

    /**
     * Tests that buyer bidding statistics are calculated correctly.
     */
    @Test
    void testBuyerBidStats() {
        // Arrange
        when(bidRepository.findByBuyerId(BUYER_ID)).thenReturn(buyerBids);
        when(orderRepository.findByUserId(BUYER_ID)).thenReturn(Collections.emptyList());

        // Act
        Map<String, Object> buyerStats = profileStatsService.getBuyerStats(BUYER_ID);

        // Assert
        assertNotNull(buyerStats);

        @SuppressWarnings("unchecked")
        Map<String, Object> biddingActivity = (Map<String, Object>) buyerStats.get("biddingActivity");

        assertNotNull(biddingActivity);
        assertEquals(TOTAL_BUYER_BIDS, biddingActivity.get("totalBids"));
        assertEquals(ACTIVE_BUYER_BIDS, biddingActivity.get("activeBids")); // PENDING + COUNTERED
        assertEquals(ACCEPTED_BUYER_BIDS, biddingActivity.get("acceptedBids")); // ACCEPTED
        assertEquals(SUCCESSFUL_BUYER_BIDS, biddingActivity.get("successfulBids")); // PAID

        // Verify repository calls
        verify(bidRepository).findByBuyerId(BUYER_ID);
    }

    /**
     * Tests that seller bidding statistics are calculated correctly.
     */
    @Test
    void testSellerBidStats() {
        // Arrange
        when(listingRepository.findBySellerId(SELLER_ID)).thenReturn(sellerListings);

        List<Long> listingIds = Arrays.asList(LISTING_ID_1, LISTING_ID_2);
        when(bidRepository.findBySellerListings(listingIds)).thenReturn(sellerReceivedBids);
        when(bidRepository.findAcceptedAndPaidBidsBySellerId(SELLER_ID)).thenReturn(
                sellerReceivedBids.stream()
                        .filter(bid -> bid.getStatus() == BidStatus.PAID)
                        .toList()
        );

        when(orderItemRepository.findByListingIdIn(listingIds)).thenReturn(Collections.emptyList());

        // Act
        Map<String, Object> sellerStats = profileStatsService.getSellerStats(SELLER_ID);

        // Assert
        assertNotNull(sellerStats);

        @SuppressWarnings("unchecked")
        Map<String, Object> bidActivity = (Map<String, Object>) sellerStats.get("bidActivity");

        assertNotNull(bidActivity);
        assertEquals(TOTAL_SELLER_BIDS, bidActivity.get("totalBidsReceived"));
        assertEquals(ACTIVE_SELLER_BIDS, bidActivity.get("activeBidsReceived")); // PENDING + COUNTERED

        @SuppressWarnings("unchecked")
        Map<String, Object> salesActivity = (Map<String, Object>) sellerStats.get("salesActivity");

        assertNotNull(salesActivity);
        // Check bid sales calculation
        BigDecimal expectedBidSales = new BigDecimal("100.0"); // 1 PAID bid at $100.0
        assertEquals(0, expectedBidSales.compareTo((BigDecimal) salesActivity.get("bidSales")));

        // Verify repository calls
        verify(listingRepository).findBySellerId(SELLER_ID);
        verify(bidRepository).findBySellerListings(listingIds);
        verify(bidRepository).findAcceptedAndPaidBidsBySellerId(SELLER_ID);
    }

    /**
     * Tests that the overall profile stats combine buyer and seller stats correctly.
     */
    @Test
    void testCombinedProfileStats() {
        // Arrange
        // Mock buyer stats creation
        Map<String, Object> buyerStats = new HashMap<>();
        Map<String, Object> biddingActivity = new HashMap<>();
        biddingActivity.put("totalBids", TOTAL_BUYER_BIDS);
        biddingActivity.put("activeBids", BID_ID_3);
        biddingActivity.put("acceptedBids", BID_ID_1);
        biddingActivity.put("successfulBids", BID_ID_2);
        buyerStats.put("biddingActivity", biddingActivity);

        // Mock seller stats creation
        Map<String, Object> sellerStats = new HashMap<>();
        Map<String, Object> sellerBidActivity = new HashMap<>();
        sellerBidActivity.put("totalBidsReceived", TOTAL_SELLER_BIDS);
        sellerBidActivity.put("activeBidsReceived", BID_ID_3);
        sellerStats.put("bidActivity", sellerBidActivity);

        // Setup ProfileStatsService spy to return our mocks
        ProfileStatsService spy = spy(profileStatsService);
        doReturn(buyerStats).when(spy).getBuyerStats(BUYER_ID);
        doReturn(sellerStats).when(spy).getSellerStats(BUYER_ID);

        // Act
        Map<String, Object> profileStats = spy.getProfileStats(BUYER_ID);

        // Assert
        assertNotNull(profileStats);
        assertTrue(profileStats.containsKey("biddingActivity"));
        assertTrue(profileStats.containsKey("bidActivity"));

        // Verify the stats were combined
        assertEquals(biddingActivity, profileStats.get("biddingActivity"));
        assertEquals(sellerBidActivity, profileStats.get("bidActivity"));

        // Verify both buyer and seller stats were fetched
        verify(spy).getBuyerStats(BUYER_ID);
        verify(spy).getSellerStats(BUYER_ID);
    }

    /**
     * Tests that bid sales are correctly calculated when there are multiple paid bids.
     */
    @Test
    void testBidSalesCalc() {
        // Arrange
        when(listingRepository.findBySellerId(SELLER_ID)).thenReturn(sellerListings);

        List<Long> listingIds = Arrays.asList(LISTING_ID_1, LISTING_ID_2);

        // Create paid bids with different prices
        List<Bid> paidBids = new ArrayList<>();
        Bid paidBid1 = createBid(BID_ID_20, BidStatus.PAID);
        paidBid1.setProposedPrice(150.0);

        Bid paidBid2 = createBid(BID_ID_21, BidStatus.PAID);
        paidBid2.setProposedPrice(200.0);

        paidBids.add(paidBid1);
        paidBids.add(paidBid2);

        when(bidRepository.findBySellerListings(listingIds)).thenReturn(sellerReceivedBids);
        when(bidRepository.findAcceptedAndPaidBidsBySellerId(SELLER_ID)).thenReturn(paidBids);

        when(orderItemRepository.findByListingIdIn(listingIds)).thenReturn(Collections.emptyList());

        // Act
        Map<String, Object> sellerStats = profileStatsService.getSellerStats(SELLER_ID);

        // Assert
        assertNotNull(sellerStats);

        @SuppressWarnings("unchecked")
        Map<String, Object> salesActivity = (Map<String, Object>) sellerStats.get("salesActivity");

        assertNotNull(salesActivity);

        // Expected bid sales: $150 + $200 = $350
        BigDecimal expectedBidSales = new BigDecimal("350.0");
        assertEquals(0, expectedBidSales.compareTo((BigDecimal) salesActivity.get("bidSales")));

        // Total sales should include bid sales
        assertEquals(0, expectedBidSales.compareTo((BigDecimal) salesActivity.get("totalSales")));

        // Items sold should include bid sales
        assertEquals(2, salesActivity.get("itemsSold"));
    }

    /**
     * Tests that empty lists are handled correctly in getBuyerStats.
     */
    @Test
    void testEmptyBuyerBids() {
        // Arrange
        when(bidRepository.findByBuyerId(BUYER_ID)).thenReturn(Collections.emptyList());
        when(orderRepository.findByUserId(BUYER_ID)).thenReturn(Collections.emptyList());

        // Act
        Map<String, Object> buyerStats = profileStatsService.getBuyerStats(BUYER_ID);

        // Assert
        assertNotNull(buyerStats);

        @SuppressWarnings("unchecked")
        Map<String, Object> biddingActivity = (Map<String, Object>) buyerStats.get("biddingActivity");

        assertNotNull(biddingActivity);
        assertEquals(0, biddingActivity.get("totalBids"));
        assertEquals(0L, biddingActivity.get("activeBids"));
        assertEquals(0L, biddingActivity.get("acceptedBids"));
        assertEquals(0L, biddingActivity.get("successfulBids"));
    }

    /**
     * Tests that empty lists are handled correctly in getSellerStats.
     */
    @Test
    void testEmptySellerListings() {
        // Arrange
        when(listingRepository.findBySellerId(SELLER_ID)).thenReturn(Collections.emptyList());

        // Act
        Map<String, Object> sellerStats = profileStatsService.getSellerStats(SELLER_ID);

        // Assert
        assertNotNull(sellerStats);

        @SuppressWarnings("unchecked")
        Map<String, Object> bidActivity = (Map<String, Object>) sellerStats.get("bidActivity");

        assertNotNull(bidActivity);
        assertEquals(0, bidActivity.get("totalBidsReceived"));
        assertEquals(0L, bidActivity.get("activeBidsReceived"));

        @SuppressWarnings("unchecked")
        Map<String, Object> salesActivity = (Map<String, Object>) sellerStats.get("salesActivity");

        assertNotNull(salesActivity);
        assertEquals(0, ((BigDecimal) salesActivity.get("bidSales")).compareTo(BigDecimal.ZERO));
    }
}