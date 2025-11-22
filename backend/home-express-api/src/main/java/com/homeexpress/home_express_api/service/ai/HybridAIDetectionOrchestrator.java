package com.homeexpress.home_express_api.service.ai;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.homeexpress.home_express_api.dto.ai.DetectionResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AI Detection Orchestrator (GPT-only)
 *
 * The system now relies solely on gpt-5-mini for visual understanding. We keep
 * the Redis cache, basic confidence checks, and manual fallback flow but remove
 * every Google/Gemini dependency to simplify the stack.
 */
@Slf4j
@RequiredArgsConstructor
public class HybridAIDetectionOrchestrator {

    private final GptService gptService;
    private final DetectionCacheService cacheService;
    private final BudgetLimitService budgetLimitService;

    @Value("${ai.detection.confidence-threshold:0.85}")
    private Double confidenceThreshold;

    @Value("${ai.detection.cache-ttl-seconds:3600}")
    private Integer cacheTtlSeconds;

    /**
     * Detect household items using GPT-5 mini, with cache + manual fallback.
     */
    public DetectionResult detectItemsHybrid(List<String> imageUrls) {
        String cacheKey = generateCacheKey(imageUrls);

        DetectionResult cachedResult = cacheService.get(cacheKey);
        if (cachedResult != null) {
            log.info("Cache hit for {} images - returning cached detection", imageUrls.size());
            ensureEnhancedItems(cachedResult);
            cachedResult.setFromCache(true);
            return cachedResult;
        }

        long startTime = System.currentTimeMillis();
        try {
            log.info("gpt-5-mini detection started - {} images", imageUrls.size());
            DetectionResult gptResult = gptService.detectItems(imageUrls);
            ensureEnhancedItems(gptResult);

            long latency = System.currentTimeMillis() - startTime;
            gptResult.setProcessingTimeMs(latency);
            gptResult.setImageCount(imageUrls.size());
            gptResult.setImageUrls(imageUrls);

            if (gptResult.getItems() == null || gptResult.getItems().isEmpty()) {
                log.warn("gpt-5-mini returned no items - manual input required");
                budgetLimitService.recordOpenAIUsage(imageUrls.size());
                return createManualInputResult(imageUrls, "NO_ITEMS_DETECTED", latency);
            }

            Double confidence = gptResult.getConfidence() != null ? gptResult.getConfidence() : 0.0;
            log.info(
                "gpt-5-mini completed - confidence: {}% - items: {} - latency: {}ms",
                String.format("%.2f", confidence * 100),
                gptResult.getItems().size(),
                latency
            );

            if (confidence < confidenceThreshold) {
                log.warn(
                    "Low confidence ({}%) from gpt-5-mini - flagging manual review",
                    String.format("%.2f", confidence * 100)
                );
                gptResult.setManualReviewRequired(true);
                gptResult.setFailureReason("OPENAI_VISION_LOW_CONFIDENCE");
            }

            cacheService.put(cacheKey, gptResult, ttlSeconds());
            budgetLimitService.recordOpenAIUsage(imageUrls.size());
            return gptResult;

        } catch (Exception e) {
            log.error("gpt-5-mini detection failed: {}", e.getMessage(), e);
            return createManualInputResult(
                imageUrls,
                "OPENAI_VISION_FAILED",
                System.currentTimeMillis() - startTime
            );
        }
    }

    private int ttlSeconds() {
        return cacheTtlSeconds != null ? cacheTtlSeconds : 3600;
    }

    /**
     * Manual fallback payload when GPT cannot help.
     */
    private DetectionResult createManualInputResult(List<String> imageUrls, String reason, long processingTime) {
        return DetectionResult.builder()
            .items(List.of())
            .confidence(0.0)
            .serviceUsed("MANUAL_INPUT_REQUIRED")
            .fallbackUsed(true)
            .manualInputRequired(true)
            .failureReason(reason)
            .processingTimeMs(processingTime)
            .imageCount(imageUrls.size())
            .imageUrls(imageUrls)
            .build();
    }

    private void ensureEnhancedItems(DetectionResult result) {
        if (result != null && result.getEnhancedItems() == null) {
            result.setEnhancedItems(List.of());
        }
    }

    private String generateCacheKey(List<String> imageUrls) {
        try {
            String combined = imageUrls.stream()
                .sorted()
                .collect(Collectors.joining("|"));

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(combined.getBytes(StandardCharsets.UTF_8));

            return "ai:detection:" + HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            return "ai:detection:" + imageUrls.hashCode();
        }
    }
}
