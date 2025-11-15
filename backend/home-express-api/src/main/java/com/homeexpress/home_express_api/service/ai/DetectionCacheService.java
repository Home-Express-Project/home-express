package com.homeexpress.home_express_api.service.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.homeexpress.home_express_api.dto.ai.DetectionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Cache service for AI detection results using Redis
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DetectionCacheService {
    
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    
    /**
     * Get cached detection result
     * 
     * @param cacheKey Cache key
     * @return DetectionResult if cached, null otherwise
     */
    public DetectionResult get(String cacheKey) {
        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            
            if (cached == null) {
                log.debug("Cache miss for key: {}", cacheKey);
                return null;
            }
            
            DetectionResult result = objectMapper.readValue(cached, DetectionResult.class);
            if (result.getEnhancedItems() == null) {
                result.setEnhancedItems(List.of());
            }
            log.info("✓ Cache hit for key: {}", cacheKey);
            
            return result;
            
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize cached result for key: {}", cacheKey, e);
            // Delete corrupted cache entry
            redisTemplate.delete(cacheKey);
            return null;
        } catch (Exception e) {
            log.error("Failed to retrieve cache for key: {}", cacheKey, e);
            return null;
        }
    }
    
    /**
     * Put detection result in cache
     * 
     * @param cacheKey Cache key
     * @param result Detection result to cache
     * @param ttlSeconds Time to live in seconds
     */
    public void put(String cacheKey, DetectionResult result, long ttlSeconds) {
        try {
            String json = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(cacheKey, json, ttlSeconds, TimeUnit.SECONDS);
            
            log.info("✓ Cached detection result for key: {} (TTL: {}s)", cacheKey, ttlSeconds);
            
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize detection result for caching: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Failed to cache detection result for key: {}", cacheKey, e);
        }
    }
    
    /**
     * Delete cached result
     * 
     * @param cacheKey Cache key
     */
    public void delete(String cacheKey) {
        try {
            redisTemplate.delete(cacheKey);
            log.info("✓ Deleted cache for key: {}", cacheKey);
        } catch (Exception e) {
            log.error("Failed to delete cache for key: {}", cacheKey, e);
        }
    }
    
    /**
     * Check if result is cached
     * 
     * @param cacheKey Cache key
     * @return true if cached, false otherwise
     */
    public boolean exists(String cacheKey) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey));
        } catch (Exception e) {
            log.error("Failed to check cache existence for key: {}", cacheKey, e);
            return false;
        }
    }
}
