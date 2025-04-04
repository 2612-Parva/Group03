package com.dalhousie.dalhousie_marketplace_backend.Service;

import com.dalhousie.dalhousie_marketplace_backend.model.*;
import com.dalhousie.dalhousie_marketplace_backend.repository.BidRepository;
import com.dalhousie.dalhousie_marketplace_backend.repository.ListingRepository;
import com.dalhousie.dalhousie_marketplace_backend.repository.OrderRepository;
import com.dalhousie.dalhousie_marketplace_backend.repository.OrderItemRepository;
import com.dalhousie.dalhousie_marketplace_backend.repository.UserRepository;
import com.dalhousie.dalhousie_marketplace_backend.service.BidService;
import com.dalhousie.dalhousie_marketplace_backend.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test class for BidService functionality.
 * Verifies bidding creation, status updates, and related operations.
 */
@ExtendWith(MockitoExtension.class)
public class BidServiceTest {

    @Mock
    private BidRepository bidRepository;

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private BidService bidService;

    private User buyer;
    private User seller;
    private Listing listing;
    private Bid bid;

    // User IDs
    private static final long TEST_BUYER_ID = 1L;
    private static final long TEST_SELLER_ID = 2L;
    private static final long NON_EXISTENT_BID_ID = 99L;

    // Listing Values
    private static final double LISTING_PRICE = 100.0;
    private static final double STARTING_BID = 50.0;
    private static final double VALID_BID_PRICE = 80.0;
    private static final double BELOW_STARTING_BID_PRICE = 40.0;
    private static final double COUNTER_BID_PRICE = 90.0;
    private static final double OTHER_BID_PRICE = 85.0;

    // Counts
    private static final int ACTIVE_BID_COUNT = 5;
    private static final int ORDER_SAVE_CALLS = 2;
    private static final int BID_SAVE_CALLS_WITH_OTHER = 3;


    /**
     * Sets up test data before each test.
     * Creates test buyer, seller, listing, and bid objects.
     */
    @BeforeEach
    void setUp() {
        buyer = new User();
        buyer.setUserId(TEST_BUYER_ID);
        buyer.setEmail("buyer@example.com");
        buyer.setusername("Buyer User");

        seller = new User();
        seller.setUserId(TEST_SELLER_ID);
        seller.setEmail("seller@example.com");
        seller.setusername("Seller User");

        listing = new Listing();
        listing.setId(TEST_BUYER_ID);
        listing.setTitle("Test Listing");
        listing.setDescription("A listing for testing");
        listing.setPrice(LISTING_PRICE);
        listing.setBiddingAllowed(true);
        listing.setStartingBid(STARTING_BID);
        listing.setSeller(seller);

        bid = new Bid();
        bid.setId(TEST_BUYER_ID);
        bid.setListing(listing);
        bid.setBuyer(buyer);
        bid.setProposedPrice(80.0);
        bid.setAdditionalTerms("I can pick it up today");
        bid.setStatus(BidStatus.PENDING);
        bid.setCreatedAt(new Date());
        bid.setUpdatedAt(new Date());
    }

    /**
     * Tests that a valid bid can be created successfully.
     */
    @Test
    void createValidBid() {
        Double proposedPrice = 80.0;
        String additionalTerms = "I can pick it up today";

        when(listingRepository.findById(listing.getId())).thenReturn(Optional.of(listing));
        when(userRepository.findById(buyer.getUserId())).thenReturn(Optional.of(buyer));
        when(bidRepository.save(any(Bid.class))).thenReturn(bid);

        Bid result = bidService.createBid(listing.getId(), buyer.getUserId(), proposedPrice, additionalTerms);

        assertNotNull(result);
        assertEquals(proposedPrice, result.getProposedPrice());
        assertEquals(additionalTerms, result.getAdditionalTerms());
        assertEquals(BidStatus.PENDING, result.getStatus());

        verify(notificationService).sendNotification(
                eq(seller),
                eq(NotificationType.BID),
                contains("New bid of $80.0 received")
        );
    }

    /**
     * Tests that bidding on a non-existent listing throws an exception.
     */
    @Test
    void bidOnNonExistentListing() {
        when(listingRepository.findById(NON_EXISTENT_BID_ID)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            bidService.createBid(NON_EXISTENT_BID_ID, buyer.getUserId(), VALID_BID_PRICE, "Terms");
        });

        assertEquals("Listing not found", exception.getMessage());
    }
    @Test
    void createBid_BelowStartingBid() {
        when(listingRepository.findById(listing.getId())).thenReturn(Optional.of(listing));
        when(userRepository.findById(buyer.getUserId())).thenReturn(Optional.of(buyer));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bidService.createBid(listing.getId(), buyer.getUserId(), BELOW_STARTING_BID_PRICE, "Terms");
        });

        assertEquals("Bid must be at least the starting price of $50.0", exception.getMessage());
    }


    /**
     * Tests that bidding is rejected when a listing doesn't allow bidding.
     */
    @Test
    void bidWhenNotAllowed() {
        listing.setBiddingAllowed(false);
        when(listingRepository.findById(listing.getId())).thenReturn(Optional.of(listing));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            bidService.createBid(listing.getId(), buyer.getUserId(), VALID_BID_PRICE, "Terms");
        });

        assertEquals("This listing does not allow bidding", exception.getMessage());
    }

    /**
     * Tests that sellers cannot bid on their own listings.
     */
    @Test
    void sellerBidOnOwnListing() {
        when(listingRepository.findById(listing.getId())).thenReturn(Optional.of(listing));
        when(userRepository.findById(seller.getUserId())).thenReturn(Optional.of(seller));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            bidService.createBid(listing.getId(), seller.getUserId(), VALID_BID_PRICE, "Terms");
        });

        assertEquals("You cannot bid on your own listing", exception.getMessage());
    }

    /**
     * Tests that a seller can accept a bid and proper notifications are sent.
     */
    @Test
    void sellerAcceptsBid() {
        when(bidRepository.findById(bid.getId())).thenReturn(Optional.of(bid));
        when(bidRepository.save(any(Bid.class))).thenReturn(bid);

        Bid result = bidService.updateBidStatus(bid.getId(), seller.getUserId(), BidStatus.ACCEPTED);

        assertNotNull(result);
        assertEquals(BidStatus.ACCEPTED, result.getStatus());

        verify(notificationService).sendNotification(
                eq(buyer),
                eq(NotificationType.BID),
                contains("was accepted")
        );
    }

    /**
     * Tests that a seller can reject a bid and proper notifications are sent.
     */
    @Test
    void sellerRejectsBid() {
        when(bidRepository.findById(bid.getId())).thenReturn(Optional.of(bid));
        when(bidRepository.save(any(Bid.class))).thenReturn(bid);

        Bid result = bidService.updateBidStatus(bid.getId(), seller.getUserId(), BidStatus.REJECTED);

        assertNotNull(result);
        assertEquals(BidStatus.REJECTED, result.getStatus());

        verify(notificationService).sendNotification(
                eq(buyer),
                eq(NotificationType.BID),
                contains("was rejected")
        );
    }

    /**
     * Tests that non-sellers cannot update bid status.
     */
    @Test
    void nonSellerUpdatesBidStatus() {
        when(bidRepository.findById(bid.getId())).thenReturn(Optional.of(bid));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            bidService.updateBidStatus(bid.getId(), buyer.getUserId(), BidStatus.ACCEPTED);
        });

        assertEquals("Only the seller can update bid status", exception.getMessage());
    }

    /**
     * Tests retrieving all bids for a specific listing.
     */
    @Test
    void getBidsForListing() {
        List<Bid> expectedBids = Arrays.asList(bid);
        when(bidRepository.findByListingId(listing.getId())).thenReturn(expectedBids);

        List<Bid> result = bidService.getBidsByListing(listing.getId());

        assertEquals(expectedBids.size(), result.size());
        assertEquals(expectedBids.get(0), result.get(0));
    }

    /**
     * Tests retrieving all bids made by a specific user.
     */
    @Test
    void getBidsForUser() {
        List<Bid> expectedBids = Arrays.asList(bid);
        when(bidRepository.findByBuyerId(buyer.getUserId())).thenReturn(expectedBids);

        List<Bid> result = bidService.getBidsByUser(buyer.getUserId());

        assertEquals(expectedBids.size(), result.size());
        assertEquals(expectedBids.get(0), result.get(0));
    }

    /**
     * Tests retrieving bids with a specific status for a listing.
     */
    @Test
    void getPendingBids() {
        List<Bid> expectedBids = Arrays.asList(bid);
        when(bidRepository.findByListingIdAndStatus(listing.getId(), BidStatus.PENDING)).thenReturn(expectedBids);

        List<Bid> result = bidService.getBidsByListingAndStatus(listing.getId(), BidStatus.PENDING);

        assertEquals(expectedBids.size(), result.size());
        assertEquals(expectedBids.get(0), result.get(0));
    }

    /**
     * Tests that a seller can counter a bid with a new price.
     */
    @Test
    void sellerCountersBid() {
//        Double counterPrice = 90.0;
        String counterTerms = "I can do 90$ if you pick it up";

        when(bidRepository.findById(bid.getId())).thenReturn(Optional.of(bid));

        Bid counterBid = new Bid();
        counterBid.setId(TEST_SELLER_ID);
        counterBid.setListing(listing);
        counterBid.setBuyer(buyer);
        counterBid.setProposedPrice(COUNTER_BID_PRICE);
        counterBid.setAdditionalTerms(counterTerms);
        counterBid.setStatus(BidStatus.COUNTERED);

        when(bidRepository.save(any(Bid.class))).thenReturn(counterBid);

        Bid result = bidService.counterBid(bid.getId(), seller.getUserId(), COUNTER_BID_PRICE, counterTerms);

        assertNotNull(result);
        assertEquals(COUNTER_BID_PRICE, result.getProposedPrice());
        assertEquals(counterTerms, result.getAdditionalTerms());
        assertEquals(BidStatus.COUNTERED, result.getStatus());

        ArgumentCaptor<Bid> bidCaptor = ArgumentCaptor.forClass(Bid.class);
        verify(bidRepository, times(ORDER_SAVE_CALLS)).save(bidCaptor.capture());
        List<Bid> savedBids = bidCaptor.getAllValues();
        assertEquals(BidStatus.COUNTERED, savedBids.get(0).getStatus());

        verify(notificationService).sendNotification(
                eq(buyer),
                eq(NotificationType.BID),
                contains("has countered your bid")
        );
    }

    /**
     * Tests retrieving the count of active bids for a listing.
     */
    @Test
    void countActiveBids() {
        when(bidRepository.countByListingIdAndStatusIn(
                eq(listing.getId()),
                anyList()
        )).thenReturn(ACTIVE_BID_COUNT);

        int count = bidService.getActiveBidCount(listing.getId());

        assertEquals(ACTIVE_BID_COUNT, count);
    }

    @Test
    void getBidById_WhenExists() {
        when(bidRepository.findById(bid.getId())).thenReturn(Optional.of(bid));

        Bid result = bidService.getBidById(bid.getId());

        assertNotNull(result);
        assertEquals(bid.getId(), result.getId());
    }

    @Test
    void getBidById_WhenNotExists() {
        when(bidRepository.findById(NON_EXISTENT_BID_ID)).thenReturn(Optional.empty());

        Bid result = bidService.getBidById(NON_EXISTENT_BID_ID);

        assertNull(result);
    }
    @Test
    void acceptSingleBid_Success() {
        bid.setStatus(BidStatus.PENDING);
        when(bidRepository.findById(bid.getId())).thenReturn(Optional.of(bid));
        when(listingRepository.save(any(Listing.class))).thenReturn(listing);
        when(orderRepository.save(any(Order.class))).thenReturn(new Order());
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(new OrderItem());
        when(bidRepository.save(any(Bid.class))).thenReturn(bid);

        Bid result = bidService.acceptSingleBid(bid.getId(), seller.getUserId());

        assertEquals(BidStatus.ACCEPTED, result.getStatus());
        verify(notificationService, atLeastOnce()).sendNotification(any(), eq(NotificationType.BID), contains("accepted"));
    }

    @Test
    void acceptSingleBid_NonPendingBid() {
        bid.setStatus(BidStatus.REJECTED);
        when(bidRepository.findById(bid.getId())).thenReturn(Optional.of(bid));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            bidService.acceptSingleBid(bid.getId(), seller.getUserId());
        });

        assertEquals("Only pending or countered bids can be accepted", exception.getMessage());
    }

    @Test
    void acceptSingleBid_WithOtherBids() {
        Bid otherBid = new Bid();
        otherBid.setId(TEST_SELLER_ID);
        otherBid.setListing(listing);
        otherBid.setBuyer(buyer);
        otherBid.setProposedPrice(OTHER_BID_PRICE);
        otherBid.setStatus(BidStatus.PENDING);

        Order order = new Order();
        order.setOrderId(TEST_BUYER_ID);
        order.setUserId(buyer.getUserId());
        order.setTotalPrice(BigDecimal.valueOf(VALID_BID_PRICE));

        when(bidRepository.findById(bid.getId())).thenReturn(Optional.of(bid));
        when(bidRepository.findByListingIdAndStatus(listing.getId(), BidStatus.PENDING)).thenReturn(Arrays.asList(otherBid));
        when(bidRepository.findByListingIdAndStatus(listing.getId(), BidStatus.COUNTERED)).thenReturn(Collections.emptyList());
        when(listingRepository.save(any(Listing.class))).thenReturn(listing);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(new OrderItem());
        when(bidRepository.save(any(Bid.class))).thenReturn(bid).thenReturn(otherBid);

        Bid result = bidService.acceptSingleBid(bid.getId(), seller.getUserId());

        assertEquals(BidStatus.ACCEPTED, result.getStatus());
        verify(bidRepository, times(BID_SAVE_CALLS_WITH_OTHER)).save(any(Bid.class)); // Accepted bid + rejected bid + order link
        verify(notificationService, times(BID_SAVE_CALLS_WITH_OTHER)).sendNotification(any(), eq(NotificationType.BID), anyString());
    }

    @Test
    void finalizeBidding_Success() {
        Bid highestBid = new Bid();
        highestBid.setId(TEST_SELLER_ID);
        highestBid.setListing(listing);
        highestBid.setBuyer(buyer);
        highestBid.setProposedPrice(COUNTER_BID_PRICE);
        highestBid.setStatus(BidStatus.PENDING);

        Order order = new Order();
        order.setOrderId(TEST_BUYER_ID);
        order.setUserId(buyer.getUserId());
        order.setTotalPrice(BigDecimal.valueOf(COUNTER_BID_PRICE));

        when(listingRepository.findById(listing.getId())).thenReturn(Optional.of(listing));
        when(bidRepository.findByListingIdAndStatusOrderByProposedPriceDesc(listing.getId(), BidStatus.PENDING))
                .thenReturn(Arrays.asList(highestBid, bid));
        when(listingRepository.save(any(Listing.class))).thenReturn(listing);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(new OrderItem());
        when(bidRepository.save(any(Bid.class))).thenReturn(highestBid).thenReturn(bid);

        Bid result = bidService.finalizeBidding(listing.getId(), seller.getUserId());

        assertEquals(BidStatus.ACCEPTED, result.getStatus());
        verify(bidRepository, times(BID_SAVE_CALLS_WITH_OTHER)).save(any(Bid.class)); // Winner + rejected + order link
        verify(listingRepository).save(any(Listing.class));
        verify(orderRepository, times(ORDER_SAVE_CALLS)).save(any(Order.class));
        verify(notificationService, times(ORDER_SAVE_CALLS)).sendNotification(any(), eq(NotificationType.BID), anyString());
    }
    @Test
    void finalizeBidding_NoPendingBids() {
        when(listingRepository.findById(listing.getId())).thenReturn(Optional.of(listing));
        when(bidRepository.findByListingIdAndStatusOrderByProposedPriceDesc(listing.getId(), BidStatus.PENDING))
                .thenReturn(Collections.emptyList());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            bidService.finalizeBidding(listing.getId(), seller.getUserId());
        });

        assertEquals("No pending bids to finalize", exception.getMessage());
    }

    @Test
    void finalizeBidding_NonSeller() {
        when(listingRepository.findById(listing.getId())).thenReturn(Optional.of(listing));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            bidService.finalizeBidding(listing.getId(), buyer.getUserId());
        });

        assertEquals("Only the seller can finalize bidding", exception.getMessage());
    }



}