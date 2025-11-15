package com.homeexpress.home_express_api.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

/**
 * High-level OpenAI GPT Vision service.
 *
 * This is the Spring-managed bean that extends the low-level GPTVisionService
 * implementation. Other services should depend on GptService instead of
 * GPTVisionService directly.
 */
@Service
public class GptService extends GPTVisionService {

    public GptService(ObjectMapper objectMapper) {
        super(objectMapper);
    }
}

