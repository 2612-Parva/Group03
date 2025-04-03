package com.dalhousie.dalhousie_marketplace_backend.Service;

import com.dalhousie.dalhousie_marketplace_backend.DTO.ConversationDTO;
import com.dalhousie.dalhousie_marketplace_backend.model.*;
import com.dalhousie.dalhousie_marketplace_backend.repository.ListingRepository;
import com.dalhousie.dalhousie_marketplace_backend.repository.MessageRepository;
import com.dalhousie.dalhousie_marketplace_backend.repository.NotificationRepository;
import com.dalhousie.dalhousie_marketplace_backend.repository.UserRepository;
import com.dalhousie.dalhousie_marketplace_backend.service.MessageService;
import com.dalhousie.dalhousie_marketplace_backend.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private NotificationService notificationService;


    @InjectMocks
    private MessageService messageService;

    private User sender;
    private User receiver;
    private Listing mockListing;

    // Common IDs and constants to remove magic numbers
    private static final long SENDER_ID = 91L;
    private static final long BUYER_ID = 91L;
    private static final long USER_ID = 91L;
    private static final long RECEIVER_ID = 105L;
    private static final long INVALID_USER_ID = 999L;
    private static final long LISTING_ID = 1L;
    private static final long INVALID_LISTING_ID = 999L;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        sender = new User();
        sender.setUserId(SENDER_ID);
        sender.setusername("sender");

        receiver = new User();
        receiver.setUserId(RECEIVER_ID);
        receiver.setusername("receiver");

        mockListing = new Listing();
        mockListing.setId(LISTING_ID);
        mockListing.setSeller(receiver); 

        when(userRepository.findById(SENDER_ID)).thenReturn(Optional.of(sender));
        when(userRepository.findById(RECEIVER_ID)).thenReturn(Optional.of(receiver));
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(mockListing));
    }

    @Test
    public void testSendMessage_ReceiverNotFound() {
        Long senderId = SENDER_ID;
        Long listingId = LISTING_ID;
        String content = "Hello, is this available?";

        when(userRepository.findById(RECEIVER_ID)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            messageService.sendMessage(SENDER_ID, listingId, content);
        });

        assertEquals("Receiver not found", exception.getMessage());

        verify(messageRepository, never()).save(any(Message.class));
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    public void testSendMessage_ListingNotFound() {
        Long senderId = SENDER_ID;
        Long invalidListingId = INVALID_LISTING_ID;
        String content = "Hello, is this available?";

        when(listingRepository.findById(invalidListingId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            messageService.sendMessage(SENDER_ID, invalidListingId, content);
        });

        assertEquals("Listing not found", exception.getMessage());

        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    public void testGenerateConversationId() {
        String conversationId = messageService.generateConversationId(SENDER_ID, RECEIVER_ID, LISTING_ID);
        assertEquals("91_105_1", conversationId);
    }


    @Test
    public void sendMessageOverloaded_ReturnsNonNull() {
        Long senderId = SENDER_ID;
        Long listingId = LISTING_ID;
        String content = "Hello";
        String conversationId = "91_105_1";
        Message mockMessage = new Message();
        mockMessage.setConversationId(conversationId);
        when(messageRepository.save(any(Message.class))).thenReturn(mockMessage);

        Message result = messageService.sendMessage(SENDER_ID, listingId, content);

        assertNotNull(result);
    }

    @Test
    public void sendMessageOverloaded_ListingNotFound() {
        Long senderId = SENDER_ID;
        Long invalidListingId = INVALID_LISTING_ID;
        when(listingRepository.findById(invalidListingId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            messageService.sendMessage(SENDER_ID, invalidListingId, "Hello");
        });

        assertEquals("Listing not found", exception.getMessage());
    }

    // sendMessage(Long senderId, Long receiverId, Long listingId, String content, String conversationId)
    @Test
    public void sendMessage_ReturnsNonNull() {

        Long receiverId = RECEIVER_ID;
        Long listingId = LISTING_ID;
        String content = "Hi there";
        String conversationId = "91_105_1";
        Message mockMessage = new Message();
        mockMessage.setConversationId(conversationId);
        when(messageRepository.save(any(Message.class))).thenReturn(mockMessage);

        Message result = messageService.sendMessage(SENDER_ID, receiverId, listingId, content, conversationId);

        assertNotNull(result);
    }

    @Test
    public void sendMessage_SelfMessagingThrowsException() {
        String conversationId = "91_91_1";

        Exception exception = assertThrows(RuntimeException.class, () -> {
            messageService.sendMessage(USER_ID, USER_ID, LISTING_ID, "Hi me", conversationId);
        });

        assertEquals("You cannot message yourself.", exception.getMessage());
    }

    @Test
    public void sendMessage_SenderNotFound() {
        Long senderId = INVALID_USER_ID;
        Long receiverId = RECEIVER_ID;
        String conversationId = "999_105_1";
        when(userRepository.findById(INVALID_USER_ID)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            messageService.sendMessage(senderId, receiverId, LISTING_ID, "Hello", conversationId);
        });

        assertEquals("Sender not found", exception.getMessage());
    }

    @Test
    public void sendMessage_ReceiverNotFound() {

        Long receiverId = INVALID_USER_ID;
        String conversationId = "91_999_1";
        when(userRepository.findById(INVALID_USER_ID)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            messageService.sendMessage(SENDER_ID, receiverId, LISTING_ID, "Hello", conversationId);
        });

        assertEquals("Receiver not found", exception.getMessage());
    }

    @Test
    public void sendMessage_ListingNotFoundInFullMethod() {

        Long receiverId = RECEIVER_ID;
        Long invalidListingId = INVALID_LISTING_ID;
        String conversationId = "91_105_999";
        when(listingRepository.findById(invalidListingId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            messageService.sendMessage(SENDER_ID, receiverId, invalidListingId, "Hello", conversationId);
        });

        assertEquals("Listing not found", exception.getMessage());
    }

    // generateConversationId
    @Test
    public void generateConversationId_SenderLessThanReceiver() {
        String result = messageService.generateConversationId(SENDER_ID, RECEIVER_ID, LISTING_ID);

        assertEquals("91_105_1", result);
    }

    @Test
    public void generateConversationId_ReceiverLessThanSender() {
        String result = messageService.generateConversationId(RECEIVER_ID, SENDER_ID, LISTING_ID);

        assertEquals("91_105_1", result);
    }

    @Test
    public void generateConversationId_NullSenderThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            messageService.generateConversationId(null, RECEIVER_ID, LISTING_ID);
        });

        assertEquals("Sender ID cannot be null", exception.getMessage());
    }

    @Test
    public void generateConversationId_NullReceiverThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            messageService.generateConversationId(SENDER_ID, null, LISTING_ID);
        });

        assertEquals("Receiver ID cannot be null", exception.getMessage());
    }

    @Test
    public void generateConversationId_NullListingThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            messageService.generateConversationId(SENDER_ID, RECEIVER_ID, null);
        });

        assertEquals("Listing ID cannot be null", exception.getMessage());
    }

    // getMessageHistory
    @Test
    public void getMessageHistory_ReturnsCorrectSize() {
        String conversationId = "91_105_1";
        Message message1 = new Message();
        message1.setConversationId(conversationId);
        message1.setContent("Message 1");
        List<Message> mockMessages = Collections.singletonList(message1);
        when(messageRepository.findByConversationId(conversationId)).thenReturn(mockMessages);

        List<Message> result = messageService.getMessageHistory(conversationId);

        assertEquals(1, result.size());
    }

    // getSellerConversations
    @Test
    public void getSellerConversations_ReturnsCorrectSize() {
        String conversationId = "91_105_1";
        Message lastMessage = new Message();
        lastMessage.setConversationId(conversationId);
        lastMessage.setSender(sender);
        lastMessage.setReceiver(receiver);
        lastMessage.setContent("Last message");
        lastMessage.setTimestamp(new Date());
        lastMessage.setListing(mockListing);
        when(messageRepository.findDistinctConversationIdsBySeller(RECEIVER_ID)).thenReturn(Collections.singletonList(conversationId));
        when(messageRepository.findByConversationId(conversationId)).thenReturn(Collections.singletonList(lastMessage));

        List<ConversationDTO> result = messageService.getSellerConversations(RECEIVER_ID);

        assertEquals(1, result.size());
    }

    @Test
    public void getSellerConversations_EmptyWhenNoMessages() {
        when(messageRepository.findDistinctConversationIdsBySeller(RECEIVER_ID)).thenReturn(Collections.emptyList());

        List<ConversationDTO> result = messageService.getSellerConversations(RECEIVER_ID);

        assertTrue(result.isEmpty());
    }

    // getMessagesForListing
    @Test
    public void getMessagesForListing_ReturnsCorrectSize() {
        Long listingId = LISTING_ID;

        Message message = new Message();
        message.setConversationId("91_105_1");
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent("Hi");
        message.setTimestamp(new Date());
        message.setListing(mockListing);
        when(messageRepository.findByListingAndUser(listingId, USER_ID)).thenReturn(Collections.singletonList(message));

        List<ConversationDTO> result = messageService.getMessagesForListing(listingId, USER_ID);

        assertEquals(1, result.size());
    }

    @Test
    public void getMessagesForListing_EmptyWhenNoMessages() {
        Long listingId = LISTING_ID;

        when(messageRepository.findByListingAndUser(listingId, USER_ID)).thenReturn(Collections.emptyList());

        List<ConversationDTO> result = messageService.getMessagesForListing(listingId, USER_ID);

        assertTrue(result.isEmpty());
    }

    // getBuyerConversations
    @Test
    public void getBuyerConversations_ReturnsCorrectSize() {
        String conversationId = "91_105_1";
        Message lastMessage = new Message();
        lastMessage.setConversationId(conversationId);
        lastMessage.setSender(sender);
        lastMessage.setReceiver(receiver);
        lastMessage.setContent("Buyer message");
        lastMessage.setTimestamp(new Date());
        lastMessage.setListing(mockListing);
        when(messageRepository.findDistinctConversationIdsByBuyer(BUYER_ID)).thenReturn(Collections.singletonList(conversationId));
        when(messageRepository.findByConversationId(conversationId)).thenReturn(Collections.singletonList(lastMessage));

        List<ConversationDTO> result = messageService.getBuyerConversations(BUYER_ID);

        assertEquals(1, result.size());
    }

    @Test
    public void getBuyerConversations_EmptyWhenNoMessages() {
        when(messageRepository.findDistinctConversationIdsByBuyer(BUYER_ID)).thenReturn(Collections.emptyList());

        List<ConversationDTO> result = messageService.getBuyerConversations(BUYER_ID);

        assertTrue(result.isEmpty());
    }
}
