package com.homeexpress.home_express_api.service.ai;

import com.homeexpress.home_express_api.dto.ai.DetectionResult;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Public AI Detection Orchestrator (GPT-only).
 *
 * This Spring-managed bean wraps HybridAIDetectionOrchestrator and exposes a
 * simpler detectItems(...) method for controllers. The hybrid orchestrator
 * itself handles caching, confidence checks and manual fallback.
 */
@Service
public class AIDetectionOrchestrator extends HybridAIDetectionOrchestrator {

    public AIDetectionOrchestrator(GptService gptService,
                                   DetectionCacheService cacheService,
                                   BudgetLimitService budgetLimitService) {
        super(gptService, cacheService, budgetLimitService);
    }

    /**
     * Preferred entrypoint for AI detection from controllers.
     */
    public DetectionResult detectItems(List<String> imageUrls) {
        return detectItemsHybrid(imageUrls);
    }
}

