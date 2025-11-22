package com.homeexpress.home_express_api.dto;

import com.homeexpress.home_express_api.entity.CommissionRuleType;

/**
 * DTO for commission calculation results.
 * Contains the calculated commission amount, rate/fee used, and rule type.
 */
public class CommissionCalculationDTO {

    private Long commissionAmountVnd;
    private CommissionRuleType ruleType;
    private Double commissionRate;
    private Long flatFeeVnd;
    private Long agreedPriceVnd;
    private Long ruleId;

    public CommissionCalculationDTO() {
    }

    public CommissionCalculationDTO(Long commissionAmountVnd, CommissionRuleType ruleType, 
                                   Double commissionRate, Long flatFeeVnd, 
                                   Long agreedPriceVnd, Long ruleId) {
        this.commissionAmountVnd = commissionAmountVnd;
        this.ruleType = ruleType;
        this.commissionRate = commissionRate;
        this.flatFeeVnd = flatFeeVnd;
        this.agreedPriceVnd = agreedPriceVnd;
        this.ruleId = ruleId;
    }

    public Long getCommissionAmountVnd() {
        return commissionAmountVnd;
    }

    public void setCommissionAmountVnd(Long commissionAmountVnd) {
        this.commissionAmountVnd = commissionAmountVnd;
    }

    public CommissionRuleType getRuleType() {
        return ruleType;
    }

    public void setRuleType(CommissionRuleType ruleType) {
        this.ruleType = ruleType;
    }

    public Double getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(Double commissionRate) {
        this.commissionRate = commissionRate;
    }

    public Long getFlatFeeVnd() {
        return flatFeeVnd;
    }

    public void setFlatFeeVnd(Long flatFeeVnd) {
        this.flatFeeVnd = flatFeeVnd;
    }

    public Long getAgreedPriceVnd() {
        return agreedPriceVnd;
    }

    public void setAgreedPriceVnd(Long agreedPriceVnd) {
        this.agreedPriceVnd = agreedPriceVnd;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }
}
