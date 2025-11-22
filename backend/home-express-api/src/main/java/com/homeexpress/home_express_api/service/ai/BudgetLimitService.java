package com.homeexpress.home_express_api.service.ai;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.homeexpress.home_express_api.dto.ai.BudgetStats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Budget tracker for the GPT-5 mini vision workload.
 *
 * We keep simple hourly/daily counters plus a rough cost estimate so operators
 * can monitor OpenAI usage from the API.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetLimitService {

    private final StringRedisTemplate redisTemplate;

    @Value("${ai.budget.openai.max-requests-per-hour:300}")
    private Integer maxOpenAiRequestsPerHour;

    @Value("${ai.budget.openai.max-requests-per-day:3000}")
    private Integer maxOpenAiRequestsPerDay;

    @Value("${ai.budget.openai.max-cost-per-day:150.0}")
    private Double maxOpenAiCostPerDay;

    @Value("${ai.budget.openai.cost-per-image:0.01}")
    private Double openAiCostPerImage;

    /**
     * Record a batch of OpenAI image analyses.
     */
    public void recordOpenAIUsage(int imageCount) {
        if (imageCount <= 0) {
            return;
        }

        String hourKey = getHourlyKey();
        String dayKey = getDailyKey();

        Long hourlyCount = incrementCounter(hourKey, imageCount, 1, TimeUnit.HOURS);
        Long dailyCount = incrementCounter(dayKey, imageCount, 1, TimeUnit.DAYS);

        double hourlyCost = hourlyCount * openAiCostPerImage;
        double dailyCost = dailyCount * openAiCostPerImage;

        log.info(
            "OpenAI usage recorded - hour: {} images (${}) | day: {} images (${})",
            hourlyCount,
            String.format("%.2f", hourlyCost),
            dailyCount,
            String.format("%.2f", dailyCost)
        );

        if (dailyCost >= maxOpenAiCostPerDay * 0.8) {
            log.warn(
                "OpenAI daily cost approaching limit: ${} / ${}",
                String.format("%.2f", dailyCost),
                String.format("%.2f", maxOpenAiCostPerDay)
            );
        }

        if (dailyCount >= maxOpenAiRequestsPerDay * 0.8) {
            log.warn(
                "OpenAI daily request volume approaching limit: {} / {}",
                dailyCount,
                maxOpenAiRequestsPerDay
            );
        }
    }

    /**
     * Current usage snapshot for dashboards/health checks.
     */
    public BudgetStats getOpenAIStats() {
        String hourKey = getHourlyKey();
        String dayKey = getDailyKey();

        Integer hourlyCount = getCount(hourKey);
        Integer dailyCount = getCount(dayKey);

        int hourValue = hourlyCount != null ? hourlyCount : 0;
        int dayValue = dailyCount != null ? dailyCount : 0;

        return BudgetStats.builder()
            .hourlyRequests(hourValue)
            .dailyRequests(dayValue)
            .hourlyCost(hourValue * openAiCostPerImage)
            .dailyCost(dayValue * openAiCostPerImage)
            .hourlyLimit(maxOpenAiRequestsPerHour)
            .dailyLimit(maxOpenAiRequestsPerDay)
            .dailyCostLimit(maxOpenAiCostPerDay)
            .build();
    }

    public void resetHourlyCounter() {
        redisTemplate.delete(getHourlyKey());
        log.info("Reset OpenAI hourly usage counter");
    }

    public void resetDailyCounter() {
        redisTemplate.delete(getDailyKey());
        log.info("Reset OpenAI daily usage counter");
    }

    // ----------------------------------------------------------------------
    // Internal helpers
    // ----------------------------------------------------------------------

    private String getHourlyKey() {
        LocalDateTime now = LocalDateTime.now();
        return String.format("openai:usage:hour:%s:%d", now.toLocalDate(), now.getHour());
    }

    private String getDailyKey() {
        return String.format("openai:usage:day:%s", LocalDate.now());
    }

    private Integer getCount(String key) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            return value != null ? Integer.parseInt(value) : null;
        } catch (Exception e) {
            log.error("Failed to parse counter for key {}", key, e);
            return null;
        }
    }

    private Long incrementCounter(String key, int amount, long ttl, TimeUnit ttlUnit) {
        try {
            Long newValue = redisTemplate.opsForValue().increment(key, amount);
            if (newValue != null && newValue == amount) {
                redisTemplate.expire(key, ttl, ttlUnit);
            }
            return newValue != null ? newValue : 0L;
        } catch (Exception e) {
            log.error("Failed to increment counter {} by {}", key, amount, e);
            return 0L;
        }
    }
}
