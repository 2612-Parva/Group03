package com.dalhousie.dalhousie_marketplace_backend.Service;

import com.dalhousie.dalhousie_marketplace_backend.model.Listing;
import com.dalhousie.dalhousie_marketplace_backend.model.ListingImage;
import com.dalhousie.dalhousie_marketplace_backend.repository.ListingImageRepository;
import com.dalhousie.dalhousie_marketplace_backend.repository.ListingRepository;
import com.dalhousie.dalhousie_marketplace_backend.service.ListingImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for ListingImageService to ensure full coverage of saveImage method
 */
@ExtendWith(MockitoExtension.class)
public class ListingImageServiceTest {

    @Mock
    private ListingImageRepository listingImageRepository;

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private MultipartFile imageFile;

    @InjectMocks
    private ListingImageService listingImageService;

    private Listing listing;
    private Long sellerId;
    private byte[] imageData;

    private static final long TEST_LISTING_ID = 1L;
    private static final long TEST_SELLER_ID = 2L;
    private static final long NON_EXISTENT_LISTING_ID = 99L;

    private static final double TEST_LISTING_PRICE = 100.0;
    private static final long TEST_IMAGE_SIZE = 4L;



    @BeforeEach
    void setUp() throws IOException {
        // Sample listing
        listing = new Listing();
        listing.setId(TEST_LISTING_ID);
        listing.setTitle("Test Listing");
        listing.setPrice(TEST_LISTING_PRICE);
        listing.setStatus(Listing.ListingStatus.ACTIVE);
        listing.setCreatedAt(new Date());
        listing.setUpdatedAt(new Date());

        sellerId = TEST_SELLER_ID;
        imageData = new byte[]{1, 2, 3, 4}; // Sample image data

//        // Mock MultipartFile behavior
//        when(imageFile.getBytes()).thenReturn(imageData);
//        when(imageFile.getContentType()).thenReturn("image/jpeg");
//        when(imageFile.getSize()).thenReturn(TEST_IMAGE_SIZE);
    }

    @Test
    void saveImage_ReturnsNonNullResult() throws IOException {
        when(imageFile.getBytes()).thenReturn(imageData);
        when(imageFile.getContentType()).thenReturn("image/jpeg");
        when(imageFile.getSize()).thenReturn(TEST_IMAGE_SIZE);
        when(listingRepository.findById(listing.getId())).thenReturn(Optional.of(listing));
        ListingImage savedImage = new ListingImage();
        savedImage.setId(TEST_LISTING_ID);
        savedImage.setListing(listing);
        savedImage.setSellerId(sellerId);
        savedImage.setImageData(imageData);
        savedImage.setImageType("image/jpeg");
        savedImage.setImageSize(TEST_IMAGE_SIZE);
        savedImage.setIsPrimary(true);
        when(listingImageRepository.save(any(ListingImage.class))).thenReturn(savedImage);

        ListingImage result = listingImageService.saveImage(listing.getId(), imageFile, sellerId, true);

        assertNotNull(result);
        verify(listingRepository).findById(listing.getId());
        verify(listingImageRepository).save(any(ListingImage.class));
    }

    @Test
    void saveImage_SetsCorrectListing() throws IOException {
        when(imageFile.getBytes()).thenReturn(imageData);
        when(imageFile.getContentType()).thenReturn("image/jpeg");
        when(imageFile.getSize()).thenReturn(TEST_IMAGE_SIZE);
        when(listingRepository.findById(listing.getId())).thenReturn(Optional.of(listing));
        ListingImage savedImage = new ListingImage();
        savedImage.setId(TEST_LISTING_ID);
        savedImage.setListing(listing);
        savedImage.setSellerId(sellerId);
        savedImage.setImageData(imageData);
        savedImage.setImageType("image/jpeg");
        savedImage.setImageSize(TEST_IMAGE_SIZE);
        savedImage.setIsPrimary(true);
        when(listingImageRepository.save(any(ListingImage.class))).thenReturn(savedImage);

        ListingImage result = listingImageService.saveImage(listing.getId(), imageFile, sellerId, true);

        assertEquals(listing, result.getListing());
        verify(listingRepository).findById(listing.getId());
        verify(listingImageRepository).save(any(ListingImage.class));
    }

    @Test
    void saveImage_SetsCorrectSellerId() throws IOException {
        when(imageFile.getBytes()).thenReturn(imageData);
        when(imageFile.getContentType()).thenReturn("image/jpeg");
        when(imageFile.getSize()).thenReturn(TEST_IMAGE_SIZE);
        when(listingRepository.findById(listing.getId())).thenReturn(Optional.of(listing));
        ListingImage savedImage = new ListingImage();
        savedImage.setId(TEST_LISTING_ID);
        savedImage.setListing(listing);
        savedImage.setSellerId(sellerId);
        savedImage.setImageData(imageData);
        savedImage.setImageType("image/jpeg");
        savedImage.setImageSize(TEST_IMAGE_SIZE);
        savedImage.setIsPrimary(true);
        when(listingImageRepository.save(any(ListingImage.class))).thenReturn(savedImage);

        ListingImage result = listingImageService.saveImage(listing.getId(), imageFile, sellerId, true);

        assertEquals(sellerId, result.getSellerId());
        verify(listingRepository).findById(listing.getId());
        verify(listingImageRepository).save(any(ListingImage.class));
    }

    @Test
    void saveImage_SetsCorrectImageData() throws IOException {
        when(imageFile.getBytes()).thenReturn(imageData);
        when(imageFile.getContentType()).thenReturn("image/jpeg");
        when(imageFile.getSize()).thenReturn(TEST_IMAGE_SIZE);
        when(listingRepository.findById(listing.getId())).thenReturn(Optional.of(listing));
        ListingImage savedImage = new ListingImage();
        savedImage.setId(TEST_LISTING_ID);
        savedImage.setListing(listing);
        savedImage.setSellerId(sellerId);
        savedImage.setImageData(imageData);
        savedImage.setImageType("image/jpeg");
        savedImage.setImageSize(TEST_IMAGE_SIZE);
        savedImage.setIsPrimary(true);
        when(listingImageRepository.save(any(ListingImage.class))).thenReturn(savedImage);

        ListingImage result = listingImageService.saveImage(listing.getId(), imageFile, sellerId, true);

        assertArrayEquals(imageData, result.getImageData());
        verify(listingRepository).findById(listing.getId());
        verify(listingImageRepository).save(any(ListingImage.class));
    }

    @Test
    void saveImage_SetsCorrectImageType() throws IOException {
        when(imageFile.getBytes()).thenReturn(imageData);
        when(imageFile.getContentType()).thenReturn("image/jpeg");
        when(imageFile.getSize()).thenReturn(TEST_IMAGE_SIZE);
        when(listingRepository.findById(listing.getId())).thenReturn(Optional.of(listing));
        ListingImage savedImage = new ListingImage();
        savedImage.setId(TEST_LISTING_ID);
        savedImage.setListing(listing);
        savedImage.setSellerId(sellerId);
        savedImage.setImageData(imageData);
        savedImage.setImageType("image/jpeg");
        savedImage.setImageSize(TEST_IMAGE_SIZE);
        savedImage.setIsPrimary(true);
        when(listingImageRepository.save(any(ListingImage.class))).thenReturn(savedImage);

        ListingImage result = listingImageService.saveImage(listing.getId(), imageFile, sellerId, true);

        assertEquals("image/jpeg", result.getImageType());
        verify(listingRepository).findById(listing.getId());
        verify(listingImageRepository).save(any(ListingImage.class));
    }

    @Test
    void saveImage_SetsCorrectImageSize() throws IOException {
        when(imageFile.getBytes()).thenReturn(imageData);
        when(imageFile.getContentType()).thenReturn("image/jpeg");
        when(imageFile.getSize()).thenReturn(TEST_IMAGE_SIZE);
        when(listingRepository.findById(listing.getId())).thenReturn(Optional.of(listing));
        ListingImage savedImage = new ListingImage();
        savedImage.setId(TEST_LISTING_ID);
        savedImage.setListing(listing);
        savedImage.setSellerId(sellerId);
        savedImage.setImageData(imageData);
        savedImage.setImageType("image/jpeg");
        savedImage.setImageSize(TEST_IMAGE_SIZE);
        savedImage.setIsPrimary(true);
        when(listingImageRepository.save(any(ListingImage.class))).thenReturn(savedImage);

        ListingImage result = listingImageService.saveImage(listing.getId(), imageFile, sellerId, true);

        assertEquals(TEST_IMAGE_SIZE, result.getImageSize());
        verify(listingRepository).findById(listing.getId());
        verify(listingImageRepository).save(any(ListingImage.class));
    }

    @Test
    void saveImage_SetsCorrectIsPrimary() throws IOException {
        when(imageFile.getBytes()).thenReturn(imageData);
        when(imageFile.getContentType()).thenReturn("image/jpeg");
        when(imageFile.getSize()).thenReturn(TEST_IMAGE_SIZE);
        when(listingRepository.findById(listing.getId())).thenReturn(Optional.of(listing));
        ListingImage savedImage = new ListingImage();
        savedImage.setId(TEST_LISTING_ID);
        savedImage.setListing(listing);
        savedImage.setSellerId(sellerId);
        savedImage.setImageData(imageData);
        savedImage.setImageType("image/jpeg");
        savedImage.setImageSize(TEST_IMAGE_SIZE);
        savedImage.setIsPrimary(true);
        when(listingImageRepository.save(any(ListingImage.class))).thenReturn(savedImage);

        ListingImage result = listingImageService.saveImage(listing.getId(), imageFile, sellerId, true);

        assertTrue(result.getIsPrimary());
        verify(listingRepository).findById(listing.getId());
        verify(listingImageRepository).save(any(ListingImage.class));
    }

    @Test
    void saveImage_ListingNotFound() {
        when(listingRepository.findById(NON_EXISTENT_LISTING_ID)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            listingImageService.saveImage(NON_EXISTENT_LISTING_ID, imageFile, sellerId, true);
        });

        assertEquals("Listing not found", exception.getMessage());
        verify(listingRepository).findById(NON_EXISTENT_LISTING_ID);
        verify(listingImageRepository, never()).save(any());
    }

}