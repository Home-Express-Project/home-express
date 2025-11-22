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
 * Service Nhận diện AI Chính (Core Service).
 * 
 * Class này là trung tâm xử lý, kết hợp:
 * 1. Gọi GPT Vision để soi ảnh.
 * 2. Cache kết quả vào Redis để tiết kiệm.
 * 3. Kiểm tra độ tin cậy (Confidence) để cảnh báo nếu AI "không chắc lắm".
 * 4. Xử lý lỗi (Fallback) nếu AI sập.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIDetectionService {

    private final GPTVisionService gptService;
    private final DetectionCacheService cacheService;

    @Value("${ai.detection.confidence-threshold:0.85}")
    private Double confidenceThreshold;

    @Value("${ai.detection.cache-ttl-seconds:3600}")
    private Integer cacheTtlSeconds;

    /**
     * Hàm nhận diện đồ vật từ danh sách ảnh.
     * Quy trình: Check Cache -> Gọi AI -> Lưu Cache.
     */
    public DetectionResult detectItems(List<String> imageUrls) {
        // Tạo key cache dựa trên danh sách URL ảnh (hash SHA-256)
        String cacheKey = generateCacheKey(imageUrls);

        // 1. Kiểm tra cache trước
        DetectionResult cachedResult = cacheService.get(cacheKey);
        if (cachedResult != null) {
            log.info("Đã có cache cho {} ảnh - Trả về ngay", imageUrls.size());
            ensureEnhancedItems(cachedResult);
            cachedResult.setFromCache(true);
            return cachedResult;
        }

        long startTime = System.currentTimeMillis();
        try {
            log.info("Bắt đầu gọi AI nhận diện - {} ảnh", imageUrls.size());
            
            // 2. Gọi GPT Vision
            DetectionResult result = gptService.detectItems(imageUrls);
            ensureEnhancedItems(result);

            long latency = System.currentTimeMillis() - startTime;
            result.setProcessingTimeMs(latency);
            result.setImageCount(imageUrls.size());
            result.setImageUrls(imageUrls);

            // Nếu AI trả về rỗng -> Cần nhập tay
            if (result.getItems() == null || result.getItems().isEmpty()) {
                log.warn("AI không tìm thấy món nào - Yêu cầu nhập tay");
                return createManualInputResult(imageUrls, "NO_ITEMS_DETECTED", latency);
            }

            Double confidence = result.getConfidence() != null ? result.getConfidence() : 0.0;
            log.info(
                "AI hoàn tất - Độ tin cậy: {}% - Số lượng: {} - Thời gian: {}ms",
                String.format("%.2f", confidence * 100),
                result.getItems().size(),
                latency
            );

            // 3. Kiểm tra độ tin cậy
            if (confidence < confidenceThreshold) {
                log.warn(
                    "Độ tin cậy thấp ({}%) - Đánh dấu cần kiểm tra lại",
                    String.format("%.2f", confidence * 100)
                );
                result.setManualReviewRequired(true);
                result.setFailureReason("AI_VISION_LOW_CONFIDENCE");
            }

            // 4. Lưu vào cache
            cacheService.put(cacheKey, result, ttlSeconds());
            return result;

        } catch (Exception e) {
            log.error("Lỗi nghiêm trọng khi gọi AI: {}", e.getMessage(), e);
            // Trả về kết quả báo lỗi để Frontend biết đường xử lý (hiện form nhập tay)
            return createManualInputResult(
                imageUrls,
                "AI_VISION_FAILED",
                System.currentTimeMillis() - startTime
            );
        }
    }

    private int ttlSeconds() {
        return cacheTtlSeconds != null ? cacheTtlSeconds : 3600;
    }

    /**
     * Tạo kết quả dự phòng khi AI thất bại (Fallback).
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

    // Tạo cache key an toàn bằng cách hash danh sách URL
    private String generateCacheKey(List<String> imageUrls) {
        try {
            String combined = imageUrls.stream()
                .sorted() // Sort để thứ tự ảnh không ảnh hưởng key
                .collect(Collectors.joining("|"));

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(combined.getBytes(StandardCharsets.UTF_8));

            return "ai:detection:" + HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            return "ai:detection:" + imageUrls.hashCode();
        }
    }
}
