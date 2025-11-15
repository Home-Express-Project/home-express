package com.homeexpress.home_express_api.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * GPT-5 mini usage statistics and budget tracking.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetStats {
    
    /** Number of GPT-5 mini requests in current hour */
    private Integer hourlyRequests;
    
    /** Number of GPT-5 mini requests today */
    private Integer dailyRequests;
    
    /** GPT-5 mini cost in current hour (USD) */
    private Double hourlyCost;
    
    /** GPT-5 mini cost today (USD) */
    private Double dailyCost;
    
    /**
     * Hourly request limit
     */
    private Integer hourlyLimit;
    
    /**
     * Daily request limit
     */
    private Integer dailyLimit;
    
    /**
     * Daily cost limit (USD)
     */
    private Double dailyCostLimit;
    
    /**
     * Whether hourly limit reached
     */
    public Boolean isHourlyLimitReached() {
        return hourlyRequests >= hourlyLimit;
    }
    
    /**
     * Whether daily limit reached
     */
    public Boolean isDailyLimitReached() {
        return dailyRequests >= dailyLimit;
    }
    
    /**
     * Whether daily cost limit reached
     */
    public Boolean isDailyCostLimitReached() {
        return dailyCost >= dailyCostLimit;
    }
    
    /**
     * Remaining hourly requests
     */
    public Integer getRemainingHourlyRequests() {
        return Math.max(0, hourlyLimit - hourlyRequests);
    }
    
    /**
     * Remaining daily requests
     */
    public Integer getRemainingDailyRequests() {
        return Math.max(0, dailyLimit - dailyRequests);
    }
    
    /**
     * Remaining daily budget (USD)
     */
    public Double getRemainingDailyBudget() {
        return Math.max(0.0, dailyCostLimit - dailyCost);
    }
}
