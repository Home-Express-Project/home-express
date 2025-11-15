package com.homeexpress.home_express_api.service.ai;

import com.homeexpress.home_express_api.dto.ai.DetectionResult;
import com.homeexpress.home_express_api.dto.ai.DetectedItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIDetectionServiceTest {

    @Mock
    private GptService gptService;

    @Mock
    private DetectionCacheService cacheService;

    @Mock
    private BudgetLimitService budgetLimitService;

    private AIDetectionOrchestrator aiDetectionService;

    private List<String> mockImageUrls;
    private DetectionResult mockDetectionResult;

    @BeforeEach
    void setUp() {
        aiDetectionService = new AIDetectionOrchestrator(gptService, cacheService, budgetLimitService);
        
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
        // Given - Mock the hybrid detection to return our mock result
        AIDetectionOrchestrator spyService = spy(aiDetectionService);
        doReturn(mockDetectionResult).when(spyService).detectItemsHybrid(anyList());

        // When
        DetectionResult result = spyService.detectItems(mockImageUrls);

        // Then
        assertNotNull(result);
        assertFalse(result.getManualInputRequired());
        assertEquals("GPT", result.getServiceUsed());
        assertEquals(0.95, result.getConfidence());
        assertEquals(2, result.getItems().size());
        assertEquals("Sofa", result.getItems().get(0).getName());
        assertEquals("Furniture", result.getItems().get(0).getCategory());
    }

    @Test
    void testDetectItems_FromCache() {
        // Given - Mock cached result
        AIDetectionOrchestrator spyService = spy(aiDetectionService);
        DetectionResult cachedResult = DetectionResult.builder()
                .items(mockDetectionResult.getItems())
                .fromCache(true)
                .confidence(0.95)
                .serviceUsed("CACHE")
                .build();
        doReturn(cachedResult).when(spyService).detectItemsHybrid(anyList());

        // When
        DetectionResult result = spyService.detectItems(mockImageUrls);

        // Then
        assertNotNull(result);
        assertTrue(result.getFromCache());
        assertEquals(2, result.getItems().size());
    }

    @Test
    void testEstimateVolume_Success() {
        // Given
        AIDetectionOrchestrator spyService = spy(aiDetectionService);
        doReturn(mockDetectionResult).when(spyService).detectItemsHybrid(anyList());

        // When
        DetectionResult result = spyService.detectItems(mockImageUrls);

        // Then - Verify items detected
        assertNotNull(result);
        assertEquals(0.95, result.getConfidence());
        assertEquals(2, result.getItems().size());
        
        // Verify individual items
        assertEquals("Sofa", result.getItems().get(0).getName());
        assertEquals(0.95, result.getItems().get(0).getConfidence());
        assertEquals("Coffee Table", result.getItems().get(1).getName());
        assertEquals(0.90, result.getItems().get(1).getConfidence());
    }

    @Test
    void testDetectItems_EmptyImageList() {
        // Given
        List<String> emptyUrls = new ArrayList<>();
        AIDetectionOrchestrator spyService = spy(aiDetectionService);
        DetectionResult emptyResult = DetectionResult.builder()
                .items(new ArrayList<>())
                .manualInputRequired(true)
                .failureReason("No images provided")
                .build();
        doReturn(emptyResult).when(spyService).detectItemsHybrid(anyList());

        // When
        DetectionResult result = spyService.detectItems(emptyUrls);

        // Then
        assertNotNull(result);
        assertTrue(result.getManualInputRequired());
    }

    @Test
    void testDetectItems_BudgetExceeded() {
        // Given - Budget exceeded scenario
        AIDetectionOrchestrator spyService = spy(aiDetectionService);
        DetectionResult budgetError = DetectionResult.builder()
                .items(new ArrayList<>())
                .manualInputRequired(true)
                .failureReason("Budget limit exceeded")
                .build();
        doReturn(budgetError).when(spyService).detectItemsHybrid(anyList());

        // When
        DetectionResult result = spyService.detectItems(mockImageUrls);

        // Then
        assertNotNull(result);
        assertTrue(result.getManualInputRequired());
    }

    @Test
    void testDetectItems_LowConfidence() {
        // Given - Low confidence result
        AIDetectionOrchestrator spyService = spy(aiDetectionService);
        DetectionResult lowConfidenceResult = DetectionResult.builder()
                .items(new ArrayList<>())
                .confidence(0.45)
                .serviceUsed("GPT")
                .manualReviewRequired(true)
                .build();
        doReturn(lowConfidenceResult).when(spyService).detectItemsHybrid(anyList());

        // When
        DetectionResult result = spyService.detectItems(mockImageUrls);

        // Then
        assertNotNull(result);
        assertTrue(result.getManualReviewRequired()); // Low confidence, needs review
        assertEquals(0.45, result.getConfidence());
    }
}
