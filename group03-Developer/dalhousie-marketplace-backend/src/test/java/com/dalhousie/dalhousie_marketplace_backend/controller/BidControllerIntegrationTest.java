package com.dalhousie.dalhousie_marketplace_backend.controller;

import com.dalhousie.dalhousie_marketplace_backend.DTO.BidRequest;
import com.dalhousie.dalhousie_marketplace_backend.model.Bid;
import com.dalhousie.dalhousie_marketplace_backend.service.BidPaymentService;
import com.dalhousie.dalhousie_marketplace_backend.service.BidService;
import com.dalhousie.dalhousie_marketplace_backend.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BidControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BidService bidService;

    @Autowired
    private BidPaymentService bidPaymentService;

    @Autowired
    private ObjectMapper objectMapper;

    private String authHeader;

    private static final double TEST_BID_PRICE = 100.0;
    private static final long TEST_BID_ID = 1L;
    private static final String TEST_ADDITIONAL_TERMS = "Additional terms";

    @BeforeEach
    public void setUp() {
        authHeader = "Bearer valid-token";
        when(jwtUtil.extractUserId("valid-token")).thenReturn(TEST_BID_ID);
        when(jwtUtil.extractUsername("valid-token")).thenReturn("testuser");
        when(jwtUtil.validateToken(eq("valid-token"), anyString())).thenReturn(true);
    }

    @Test
    public void testCreateBid_Success() throws Exception {
        BidRequest bidRequest = new BidRequest();
        bidRequest.setProposedPrice(TEST_BID_PRICE);
        bidRequest.setAdditionalTerms(TEST_ADDITIONAL_TERMS);

        Bid bid = new Bid();
        bid.setId(TEST_BID_ID);
        bid.setProposedPrice(TEST_BID_PRICE);
        bid.setAdditionalTerms(TEST_ADDITIONAL_TERMS);

        when(bidService.createBid(anyLong(), anyLong(), anyDouble(), any())).thenReturn(bid);

        mockMvc.perform(post("/api/bids/1")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bidRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_BID_ID))
                .andExpect(jsonPath("$.proposedPrice").value(TEST_BID_PRICE));
    }

    @Test
    public void testGetUserBids_Success() throws Exception {
        Bid bid = new Bid();
        bid.setId(TEST_BID_ID);
        when(bidService.getBidsByUser(anyLong())).thenReturn(Collections.singletonList(bid));

        mockMvc.perform(get("/api/bids/user")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(TEST_BID_ID));
    }

    @Test
    public void testPayForAcceptedBid_Success() throws Exception {
        when(bidPaymentService.createBidCheckoutSession(anyLong(), anyLong()))
                .thenReturn("https://checkout.url");

        mockMvc.perform(post("/api/bids/1/pay")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkoutUrl").value("https://checkout.url"));
    }

    // Inject all mocks cleanly without @MockBean
    @TestConfiguration
    static class MockBeansConfig {

        @Bean
        public JwtUtil jwtUtil() {
            return mock(JwtUtil.class);
        }

        @Bean
        public BidService bidService() {
            return mock(BidService.class);
        }

        @Bean
        public BidPaymentService bidPaymentService() {
            return mock(BidPaymentService.class);
        }
    }
}
