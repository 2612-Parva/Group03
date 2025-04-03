package com.dalhousie.dalhousie_marketplace_backend.Service;

import com.dalhousie.dalhousie_marketplace_backend.model.*;
import com.dalhousie.dalhousie_marketplace_backend.repository.BidRepository;
import com.dalhousie.dalhousie_marketplace_backend.repository.ListingRepository;
import com.dalhousie.dalhousie_marketplace_backend.repository.OrderItemRepository;
import com.dalhousie.dalhousie_marketplace_backend.repository.OrderRepository;
import com.dalhousie.dalhousie_marketplace_backend.service.BidPaymentService;
import com.dalhousie.dalhousie_marketplace_backend.service.NotificationService;
import com.dalhousie.dalhousie_marketplace_backend.service.PaymentService;
import com.stripe.exception.StripeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test class for BidPaymentService functionality.
 * Verifies payment processing for bids.
 */
@ExtendWith(MockitoExtension.class)
public class BidPaymentServiceTest {

    @Mock
    private BidRepository bidRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BidPaymentService bidPaymentService;

    private User buyer;
    private User seller;
    private Listing listing;
    private Bid acceptedBid;
    private Bid pendingBid;
    private Order order;

    private static final long TEST_BUYER_ID = 1L;
    private static final long TEST_ORDER_ID = 1L;
    private static final long TEST_SELLER_ID = 2L;
    private static final long NON_EXISTENT_BID_ID = 99L;
    private static final long WRONG_USER_ID = 999L;
    private static final double LISTING_PRICE = 100.0;
    private static final double STARTING_BID = 50.0;
    private static final double ACCEPTED_BID_PRICE = 80.0;
    private static final double PENDING_BID_PRICE = 75.0;
    private static final int STRIPE_ERROR_CODE = 500;

    /**
     * Sets up test data before each test.
     * Creates test user, listing, bid, and order objects.
     */
    @BeforeEach
    void setUp() {
        // Setup buyer
        buyer = new User();
        buyer.setUserId(TEST_BUYER_ID);
        buyer.setEmail("buyer@example.com");
        buyer.setusername("Buyer User");

        // Setup seller
        seller = new User();
        seller.setUserId(TEST_SELLER_ID);
        seller.setEmail("seller@example.com");
        seller.setusername("Seller User");

        // Setup listing
        listing = new Listing();
        listing.setId(TEST_BUYER_ID);
        listing.setTitle("Test Listing");
        listing.setDescription("A listing for testing");
        listing.setPrice(LISTING_PRICE);
        listing.setBiddingAllowed(true);
        listing.setStartingBid(STARTING_BID);
        listing.setSeller(seller);

        // Setup accepted bid
        acceptedBid = new Bid();
        acceptedBid.setId(TEST_BUYER_ID);
        acceptedBid.setListing(listing);
        acceptedBid.setBuyer(buyer);
        acceptedBid.setProposedPrice(ACCEPTED_BID_PRICE);
        acceptedBid.setAdditionalTerms("I can pick it up today");
        acceptedBid.setStatus(BidStatus.ACCEPTED);
        acceptedBid.setCreatedAt(new Date());
        acceptedBid.setUpdatedAt(new Date());

        // Setup pending bid (not accepted)
        pendingBid = new Bid();
        pendingBid.setId(TEST_SELLER_ID);
        pendingBid.setListing(listing);
        pendingBid.setBuyer(buyer);
        pendingBid.setProposedPrice(PENDING_BID_PRICE);
        pendingBid.setAdditionalTerms("My offer");
        pendingBid.setStatus(BidStatus.PENDING);
        pendingBid.setCreatedAt(new Date());
        pendingBid.setUpdatedAt(new Date());

        // Setup order
        order = new Order();
        order.setOrderId(TEST_ORDER_ID);
        order.setUserId(buyer.getUserId());
        order.setTotalPrice(BigDecimal.valueOf(ACCEPTED_BID_PRICE));
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        order.setItems(new ArrayList<>());

        // Mock BidRepository in PaymentService to prevent NullPointerException
        Mockito.mock(BidRepository.class);
    }

    @Test
    void testNewCheckoutSession_Success() throws Exception {
        // Ensure seller is linked properly
        listing.setSeller(seller);
        acceptedBid.setListing(listing);
        acceptedBid.setBuyer(buyer);

        // Mock repository returns
        when(bidRepository.findById(acceptedBid.getId())).thenReturn(Optional.of(acceptedBid));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(new OrderItem());
        when(paymentService.createCheckoutSession(any(Order.class))).thenReturn("https://checkout.com/test");

        // Act
        String result = bidPaymentService.createBidCheckoutSession(acceptedBid.getId(), buyer.getUserId());

        // Assert
        assertEquals("https://checkout.com/test", result);

        // Verify interactions
        verify(orderRepository, times(2)).save(any(Order.class));
        verify(orderItemRepository).save(any(OrderItem.class));
        verify(bidRepository).save(any(Bid.class));
        verify(notificationService).sendNotification(eq(seller), eq(NotificationType.BID), contains("Payment initiated"));
    }


    @Test
    void testCheckoutWithExistingOrder_PendingStatus() throws Exception {
        acceptedBid.setOrderId(order.getOrderId());
        when(bidRepository.findById(acceptedBid.getId())).thenReturn(Optional.of(acceptedBid));
        when(orderRepository.findById(order.getOrderId())).thenReturn(Optional.of(order));
        when(paymentService.createCheckoutSession(order)).thenReturn("https://checkout.com/existing");

        String result = bidPaymentService.createBidCheckoutSession(acceptedBid.getId(), buyer.getUserId());

        assertEquals("https://checkout.com/existing", result);
        verify(orderRepository, never()).save(any(Order.class));
    }
    @Test
    void testOnlyAcceptedBidsAllowed() {
        when(bidRepository.findById(pendingBid.getId())).thenReturn(Optional.of(pendingBid));

        Exception e = assertThrows(RuntimeException.class, () -> {
            bidPaymentService.createBidCheckoutSession(pendingBid.getId(), buyer.getUserId());
        });

        assertEquals("Only accepted bids can be processed for payment", e.getMessage());
    }

    @Test
    void testWrongBuyer_NotAllowed() {
        when(bidRepository.findById(acceptedBid.getId())).thenReturn(Optional.of(acceptedBid));

        Exception e = assertThrows(RuntimeException.class, () -> {
            bidPaymentService.createBidCheckoutSession(acceptedBid.getId(), WRONG_USER_ID); // wrong user
        });

        assertEquals("You can only pay for your own bids", e.getMessage());
    }

    @Test
    void testStripeExceptionHandled() throws Exception {
        when(bidRepository.findById(acceptedBid.getId())).thenReturn(Optional.of(acceptedBid));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(new OrderItem());
        when(paymentService.createCheckoutSession(any(Order.class))).thenThrow(new StripeException("Stripe failed", null, null, STRIPE_ERROR_CODE) {});

        RuntimeException e = assertThrows(RuntimeException.class, () -> {
            bidPaymentService.createBidCheckoutSession(acceptedBid.getId(), buyer.getUserId());
        });

        assertTrue(e.getMessage().contains("Error creating payment session"));
    }

    /**
     * Tests creating a checkout session for an accepted bid with an existing order.
     */
    @Test
    void testExistingOrderCheckout() throws Exception {
        // Arrange
        String expectedCheckoutUrl = "https://stripe.com/checkout/test";
        acceptedBid.setOrderId(order.getOrderId());

        when(bidRepository.findById(acceptedBid.getId())).thenReturn(Optional.of(acceptedBid));
        when(orderRepository.findById(order.getOrderId())).thenReturn(Optional.of(order));
        when(paymentService.createCheckoutSession(order)).thenReturn(expectedCheckoutUrl);

        // Act
        String resultUrl = bidPaymentService.createBidCheckoutSession(acceptedBid.getId(), buyer.getUserId());

        // Assert
        assertEquals(expectedCheckoutUrl, resultUrl);

        // Verify the existing order was used
        verify(orderRepository, never()).save(any(Order.class));
        verify(orderItemRepository, never()).save(any(OrderItem.class));

        // Verify checkout session was created
        verify(paymentService).createCheckoutSession(eq(order));
    }

    /**
     * Tests that a non-accepted bid cannot be processed for payment.
     */
    @Test
    void testNonAcceptedBid() throws Exception {
        // Arrange
        when(bidRepository.findById(pendingBid.getId())).thenReturn(Optional.of(pendingBid));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            bidPaymentService.createBidCheckoutSession(pendingBid.getId(), buyer.getUserId());
        });

        assertEquals("Only accepted bids can be processed for payment", exception.getMessage());

        // Verify no further processing occurred
        verify(orderRepository, never()).save(any(Order.class));
        verify(paymentService, never()).createCheckoutSession(any(Order.class));
    }

    /**
     * Tests that a user can only pay for their own bids.
     */
    @Test
    void testWrongUserBid() throws Exception {
        // Arrange
        Long wrongUserId = NON_EXISTENT_BID_ID;

        when(bidRepository.findById(acceptedBid.getId())).thenReturn(Optional.of(acceptedBid));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            bidPaymentService.createBidCheckoutSession(acceptedBid.getId(), wrongUserId);
        });

        assertEquals("You can only pay for your own bids", exception.getMessage());

        // Verify no further processing occurred
        verify(orderRepository, never()).save(any(Order.class));
        verify(paymentService, never()).createCheckoutSession(any(Order.class));
    }

    /**
     * Tests handling of a bid that has been paid for already.
     */
    @Test
    void testAlreadyPaidBid() throws Exception {
        // Arrange
        acceptedBid.setOrderId(order.getOrderId());
        order.setOrderStatus(OrderStatus.COMPLETED);

        when(bidRepository.findById(acceptedBid.getId())).thenReturn(Optional.of(acceptedBid));
        when(orderRepository.findById(order.getOrderId())).thenReturn(Optional.of(order));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            bidPaymentService.createBidCheckoutSession(acceptedBid.getId(), buyer.getUserId());
        });

        assertEquals("This bid has already been paid for", exception.getMessage());

        // Verify no further processing occurred
        verify(paymentService, never()).createCheckoutSession(any(Order.class));
    }


    /**
     * Tests that a bid not found scenario is handled properly.
     */
    @Test
    void testBidNotFound() throws Exception {
        // Arrange
        when(bidRepository.findById(NON_EXISTENT_BID_ID)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            bidPaymentService.createBidCheckoutSession(NON_EXISTENT_BID_ID, buyer.getUserId());
        });

        assertEquals("Bid not found", exception.getMessage());

        // Verify no further processing
        verify(orderRepository, never()).save(any(Order.class));
        verify(paymentService, never()).createCheckoutSession(any(Order.class));
    }
}