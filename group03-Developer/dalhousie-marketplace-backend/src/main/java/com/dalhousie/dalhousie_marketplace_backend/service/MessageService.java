package com.dalhousie.dalhousie_marketplace_backend.service;

import com.dalhousie.dalhousie_marketplace_backend.DTO.ConversationDTO;
import com.dalhousie.dalhousie_marketplace_backend.model.*;
import com.dalhousie.dalhousie_marketplace_backend.repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;


interface MessageSubject {
    void registerObserver(MessageObserver observer);
    void removeObserver(MessageObserver observer);
    void notifyObservers(Message message);
}

interface MessageObserver {
    void update(Message message);
}

@Service
@Scope("singleton")
public class MessageService implements MessageSubject {
    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private List<MessageObserver> observers = new ArrayList<>();

    @PostConstruct
    public void init() {
        registerObserver(new NotificationObserver());
        registerObserver(new WebSocketObserver());
    }

    @Override
    public void registerObserver(MessageObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(MessageObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(Message message) {
        for (MessageObserver observer : observers) {
            observer.update(message);
        }
    }

    @Component
    public class NotificationObserver implements MessageObserver {
        @Autowired
        private NotificationService notificationService;

        @Override
        public void update(Message message) {
            User receiver = message.getReceiver();
            String content = message.getSender().getUsername() + " sent you a message: " + message.getContent();

            notificationService.sendNotification(
                    receiver,
                    NotificationType.MESSAGE,
                    content
            );
        }
    }

    @Component
    public class WebSocketObserver implements MessageObserver {
        @Autowired
        private SimpMessagingTemplate messagingTemplate;

        @Override
        public void update(Message message) {
            // Send real-time update via WebSocket
            messagingTemplate.convertAndSendToUser(
                    message.getReceiver().getUserId().toString(),
                    "/queue/messages",
                    message
            );
        }
    }


    public Message sendMessage(Long senderId, Long listingId, String content) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Listing not found"));

        Long receiverId = listing.getSeller().getUserId();
        String conversationId = generateConversationId(senderId, receiverId, listingId);

        return sendMessage(senderId, receiverId, listingId, content, conversationId);
    }


    public Message sendMessage(Long senderId, Long receiverId, Long listingId, String content, String conversationId) {
        if (senderId.equals(receiverId)) {
            throw new RuntimeException("You cannot message yourself.");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Listing not found"));

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);
        message.setListing(listing);
        message.setConversationId(conversationId);
        message.setTimestamp(new Date());

        messageRepository.save(message);


        notifyObservers(message);

        return message;
    }


    public String generateConversationId(Long senderId, Long receiverId, Long listingId) {
        validateNotNull(senderId, "Sender ID");
        validateNotNull(receiverId, "Receiver ID");
        validateNotNull(listingId, "Listing ID");

        Long firstId = Math.min(senderId, receiverId);
        Long secondId = Math.max(senderId, receiverId);

        return firstId + "_" + secondId + "_" + listingId;
    }

    private void validateNotNull(Long value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
    }


    public List<Message> getMessageHistory(String conversationId) {
        return messageRepository.findByConversationId(conversationId);
    }


    public List<ConversationDTO> getSellerConversations(Long sellerId) {
        List<String> conversationIds = messageRepository.findDistinctConversationIdsBySeller(sellerId);
        return buildConversationDTOList(conversationIds, sellerId);
    }

    private List<ConversationDTO> buildConversationDTOList(List<String> conversationIds, Long sellerId) {
        List<ConversationDTO> conversations = new ArrayList<>();
        for (String conversationId : conversationIds) {
            ConversationDTO dto = buildSellerConversationDTO(conversationId, sellerId);
            if (dto != null) {
                conversations.add(dto);
            }
        }
        return conversations;
    }

    private ConversationDTO buildSellerConversationDTO(String conversationId, Long sellerId) {
        List<Message> messages = fetchMessages(conversationId);
        if (messages.isEmpty()) {
            return null;
        }

        Message lastMessage = getLastMessage(messages);
        User otherParty = determineOtherParty(lastMessage, sellerId);
        Listing listing = lastMessage.getListing();

        return createConversationDTO(conversationId, otherParty, lastMessage, listing);
    }

    private List<Message> fetchMessages(String conversationId) {
        return messageRepository.findByConversationId(conversationId);
    }

    private Message getLastMessage(List<Message> messages) {
        return messages.get(messages.size() - 1);
    }

    private User determineOtherParty(Message message, Long sellerId) {
        return message.getSender().getUserId().equals(sellerId)
                ? message.getReceiver()
                : message.getSender();
    }

    private ConversationDTO createConversationDTO(String conversationId, User otherParty,
                                                  Message lastMessage, Listing listing) {
        return new ConversationDTO(
                conversationId,
                otherParty.getUserId(),
                otherParty.getUsername(),
                lastMessage.getContent(),
                lastMessage.getTimestamp().toString(),
                listing.getId(),
                listing.getTitle()
        );
    }


    public List<ConversationDTO> getMessagesForListing(Long listingId, Long userId) {
        List<Message> messages = messageRepository.findByListingAndUser(listingId, userId);
        Map<String, Message> latestMessages = new HashMap<>();

        for (Message msg : messages) {
            String conversationId = msg.getConversationId();
            boolean isNewer = !latestMessages.containsKey(conversationId) ||
                    msg.getTimestamp().after(latestMessages.get(conversationId).getTimestamp());

            if (isNewer) {
                latestMessages.put(conversationId, msg);
            }
        }

        return latestMessages.values().stream()
                .map(msg -> {
                    boolean isSender = msg.getSender().getUserId().equals(userId);
                    User otherParty = isSender ? msg.getReceiver() : msg.getSender();

                    String conversationId = msg.getConversationId();
                    Long otherUserId = otherParty.getUserId();
                    String otherUsername = otherParty.getUsername();
                    String content = msg.getContent();
                    String timestamp = msg.getTimestamp().toString();
                    Long msgListingId = msg.getListing().getId();
                    String listingTitle = msg.getListing().getTitle();

                    return new ConversationDTO(
                            conversationId,
                            otherUserId,
                            otherUsername,
                            content,
                            timestamp,
                            msgListingId,
                            listingTitle
                    );
                })
                .collect(Collectors.toList());
    }


    public List<ConversationDTO> getBuyerConversations(Long buyerId) {
        List<String> conversationIds = messageRepository.findDistinctConversationIdsByBuyer(buyerId);

        return conversationIds.stream()
                .map(conversationId -> {
                    List<Message> messages = messageRepository.findByConversationId(conversationId);
                    if (messages.isEmpty()) return null;

                    Message lastMessage = messages.get(messages.size() - 1);
                    Listing listing = lastMessage.getListing();
                    User seller = listing.getSeller();

                    Long sellerId = seller.getUserId();
                    String sellerUsername = seller.getUsername();
                    String content = lastMessage.getContent();
                    String timestamp = lastMessage.getTimestamp().toString();
                    Long listingId = listing.getId();
                    String listingTitle = listing.getTitle();

                    return new ConversationDTO(
                            conversationId,
                            sellerId,
                            sellerUsername,
                            content,
                            timestamp,
                            listingId,
                            listingTitle
                    );
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}