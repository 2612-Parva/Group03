package com.dalhousie.dalhousie_marketplace_backend.controller;

import com.dalhousie.dalhousie_marketplace_backend.model.Listing;
import com.dalhousie.dalhousie_marketplace_backend.service.ListingService;
import com.dalhousie.dalhousie_marketplace_backend.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

import static org.mockito.Mockito.*;
        import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(BiddingListingsControllerIntegrationTest.TestConfig.class)
@ActiveProfiles("test")
public class BiddingListingsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ListingService listingService;

    @Autowired
    private JwtUtil jwtUtil;

    private String authHeader;

    private static final long TEST_USER_ID = 1L;
    private static final long TEST_LISTING_ID_SELLER = 1L;
    private static final long TEST_LISTING_ID_MY = 2L;

    @BeforeEach
    public void setup() {
        authHeader = "Bearer valid-token";
        when(jwtUtil.extractUserId("valid-token")).thenReturn(TEST_USER_ID);
    }

    @Test
    public void testGetBiddingListingsBySeller() throws Exception {
        Listing listing = new Listing();
        listing.setId(TEST_LISTING_ID_SELLER);

        when(listingService.getBiddingListingsBySeller(TEST_LISTING_ID_SELLER)).thenReturn(Arrays.asList(listing));

        mockMvc.perform(get("/api/listings/bidding/seller/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(TEST_LISTING_ID_SELLER));
    }

    @Test
    public void testGetMyBiddingListings() throws Exception {
        Listing listing = new Listing();
        listing.setId(TEST_LISTING_ID_MY);

        when(listingService.getBiddingListingsBySeller(TEST_LISTING_ID_SELLER)).thenReturn(Arrays.asList(listing));

        mockMvc.perform(get("/api/listings/bidding/my-listings")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(TEST_LISTING_ID_MY));
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public ListingService listingService() {
            return mock(ListingService.class);
        }

        @Bean
        public JwtUtil jwtUtil() {
            return mock(JwtUtil.class);
        }
    }
}
