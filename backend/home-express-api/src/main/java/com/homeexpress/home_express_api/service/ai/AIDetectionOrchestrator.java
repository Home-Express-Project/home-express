package com.homeexpress.home_express_api.service.ai;

import com.homeexpress.home_express_api.dto.ai.DetectionResult;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Bộ điều phối AI (Orchestrator).
 * 
 * Đây là điểm tiếp nhận duy nhất (Entrypoint) cho các yêu cầu nhận diện hình ảnh từ Controller.
 * Nó giúp che giấu sự phức tạp bên dưới (GPT, Cache, Fallback...).
 * Hiện tại nó chỉ gọi GPT, nhưng sau này có thể mở rộng để gọi thêm Google Vision hoặc model khác song song.
 */
@Service
public class AIDetectionOrchestrator {

    private final AIDetectionService aiDetectionService;

    public AIDetectionOrchestrator(AIDetectionService aiDetectionService) {
        this.aiDetectionService = aiDetectionService;
    }

    /**
     * Hàm gọi AI để nhận diện đồ vật từ danh sách ảnh.
     */
    public DetectionResult detectItems(List<String> imageUrls) {
        // Có thể thêm logic tiền xử lý ảnh tại đây (resize, validate...) trước khi gọi Service
        return aiDetectionService.detectItems(imageUrls);
    }
}
