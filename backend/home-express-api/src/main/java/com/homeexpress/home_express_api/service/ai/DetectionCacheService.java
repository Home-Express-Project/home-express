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
 * Service lưu trữ tạm thời (Cache) kết quả AI bằng Redis.
 * Giúp tiết kiệm tiền và thời gian: Nếu cùng một bộ ảnh được gửi lên lần 2,
 * hệ thống sẽ trả về kết quả cũ ngay lập tức thay vì gọi OpenAI lại.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DetectionCacheService {
    
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    
    /**
     * Lấy kết quả đã lưu trong Cache (nếu có).
     * @param cacheKey Khóa định danh (thường là mã hash của danh sách URL ảnh)
     */
    public DetectionResult get(String cacheKey) {
        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            
            if (cached == null) {
                log.debug("Không tìm thấy cache cho key: {}", cacheKey);
                return null;
            }
            
            DetectionResult result = objectMapper.readValue(cached, DetectionResult.class);
            if (result.getEnhancedItems() == null) {
                result.setEnhancedItems(List.of());
            }
            log.info("✓ Lấy thành công kết quả từ Cache (Key: {})", cacheKey);
            
            return result;
            
        } catch (JsonProcessingException e) {
            log.error("Lỗi đọc dữ liệu cache (JSON hỏng): {}", cacheKey, e);
            // Nếu dữ liệu hỏng thì xóa luôn để lần sau lưu cái mới
            redisTemplate.delete(cacheKey);
            return null;
        } catch (Exception e) {
            log.error("Lỗi kết nối Redis khi lấy cache: {}", cacheKey, e);
            return null;
        }
    }
    
    /**
     * Lưu kết quả AI vào Cache.
     * @param ttlSeconds Thời gian sống (Time To Live). Sau thời gian này cache sẽ tự hủy.
     */
    public void put(String cacheKey, DetectionResult result, long ttlSeconds) {
        try {
            String json = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(cacheKey, json, ttlSeconds, TimeUnit.SECONDS);
            
            log.info("✓ Đã lưu kết quả vào Cache (Key: {}, TTL: {}s)", cacheKey, ttlSeconds);
            
        } catch (JsonProcessingException e) {
            log.error("Lỗi đóng gói dữ liệu để cache: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Lỗi kết nối Redis khi lưu cache: {}", cacheKey, e);
        }
    }
    
    /**
     * Xóa cache thủ công (ít dùng, thường để hệ thống tự hủy theo TTL).
     */
    public void delete(String cacheKey) {
        try {
            redisTemplate.delete(cacheKey);
            log.info("✓ Đã xóa cache: {}", cacheKey);
        } catch (Exception e) {
            log.error("Lỗi khi xóa cache: {}", cacheKey, e);
        }
    }
    
    public boolean exists(String cacheKey) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey));
        } catch (Exception e) {
            log.error("Lỗi kiểm tra cache tồn tại: {}", cacheKey, e);
            return false;
        }
    }
}
