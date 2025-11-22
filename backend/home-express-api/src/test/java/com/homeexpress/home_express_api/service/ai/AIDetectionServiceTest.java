package com.homeexpress.home_express_api.service.ai;

import com.homeexpress.home_express_api.dto.ai.DetectionResult;
import com.homeexpress.home_express_api.dto.ai.DetectedItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIDetectionServiceTest {

    @Mock
    private GPTVisionService gptService;

    @Mock
    private DetectionCacheService cacheService;

    private AIDetectionService aiDetectionService;

    private List<String> mockImageUrls;
    private DetectionResult mockDetectionResult;

    @BeforeEach
    void setUp() {
        aiDetectionService = new AIDetectionService(gptService, cacheService);
        // Inject values for @Value fields
        ReflectionTestUtils.setField(aiDetectionService, "confidenceThreshold", 0.85);
        ReflectionTestUtils.setField(aiDetectionService, "cacheTtlSeconds", 3600);
        
        // Setup mock image URLs
        mockImageUrls = Arrays.asList(
                "https://example.com/image1.jpg",
                "https://example.com/image2.jpg"
        );

        // Setup mock detection result
        List<DetectedItem> items = new ArrayList<>();
        DetectedItem item1 = DetectedItem.builder()
                .name("Sofa")
                .category("Furniture")
                .confidence(0.95)
                .build();
        items.add(item1);

        DetectedItem item2 = DetectedItem.builder()
                .name("Coffee Table")
                .category("Furniture")
                .confidence(0.90)
                .build();
        items.add(item2);

        mockDetectionResult = DetectionResult.builder()
                .items(items)
                .confidence(0.95)
                .serviceUsed("GPT")
                .fallbackUsed(false)
                .manualInputRequired(false)
                .build();
    }

    @Test
    void testDetectItems_Success() {
        // Given - Cache miss, GPT success
        when(cacheService.get(anyString())).thenReturn(null);
        when(gptService.detectItems(anyList())).thenReturn(mockDetectionResult);

        // When
        DetectionResult result = aiDetectionService.detectItems(mockImageUrls);

        // Then
        assertNotNull(result);
        assertFalse(result.getManualInputRequired());
        assertEquals("GPT", result.getServiceUsed());
        assertEquals(0.95, result.getConfidence());
        assertEquals(2, result.getItems().size());
        assertEquals("Sofa", result.getItems().get(0).getName());
        
        verify(gptService).detectItems(anyList());
        verify(cacheService).put(anyString(), any(DetectionResult.class), anyLong());
    }

    @Test
    void testDetectItems_FromCache() {
        // Given - Cache hit
        DetectionResult cachedResult = DetectionResult.builder()
                .items(mockDetectionResult.getItems())
                .fromCache(true)
                .confidence(0.95)
                .serviceUsed("GPT")
                .build();
        when(cacheService.get(anyString())).thenReturn(cachedResult);

        // When
        DetectionResult result = aiDetectionService.detectItems(mockImageUrls);

        // Then
        assertNotNull(result);
        assertTrue(result.getFromCache());
        assertEquals(2, result.getItems().size());
        
        verify(gptService, never()).detectItems(anyList());
    }

    @Test
    void testDetectItems_EmptyImageList() {
        // Given - GPT returns empty items
        DetectionResult emptyResult = DetectionResult.builder()
             .items(new ArrayList<>())
             .confidence(0.0)
             .build();
             
        when(cacheService.get(anyString())).thenReturn(null);
        when(gptService.detectItems(anyList())).thenReturn(emptyResult);

        // When
        DetectionResult result = aiDetectionService.detectItems(mockImageUrls);

        // Then
        assertTrue(result.getManualInputRequired());
        assertEquals("NO_ITEMS_DETECTED", result.getFailureReason());
    }

    @Test
    void testDetectItems_LowConfidence() {
        // Given - Low confidence result from GPT
        DetectionResult lowConfidenceResult = DetectionResult.builder()
                .items(mockDetectionResult.getItems())
                .confidence(0.45)
                .serviceUsed("GPT")
                .build();
                
        when(cacheService.get(anyString())).thenReturn(null);
        when(gptService.detectItems(anyList())).thenReturn(lowConfidenceResult);

        // When
        DetectionResult result = aiDetectionService.detectItems(mockImageUrls);

        // Then
        assertNotNull(result);
        assertTrue(result.getManualReviewRequired());
        assertEquals("AI_VISION_LOW_CONFIDENCE", result.getFailureReason());
    }
    
    @Test
    void testDetectItems_GptFailure() {
        // Given - GPT throws exception
        when(cacheService.get(anyString())).thenReturn(null);
        when(gptService.detectItems(anyList())).thenThrow(new RuntimeException("API Error"));

        // When
        DetectionResult result = aiDetectionService.detectItems(mockImageUrls);

        // Then
        assertTrue(result.getManualInputRequired());
        assertEquals("AI_VISION_FAILED", result.getFailureReason());
    }
}
