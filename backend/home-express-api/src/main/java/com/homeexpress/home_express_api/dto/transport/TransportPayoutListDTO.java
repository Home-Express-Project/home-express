package com.homeexpress.home_express_api.dto.transport;

import com.homeexpress.home_express_api.entity.PayoutStatus;

import java.time.LocalDateTime;

public class TransportPayoutListDTO {

    private Long payoutId;
    private String payoutNumber;
    private Long totalAmountVnd;
    private Integer itemCount;
    private PayoutStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private LocalDateTime completedAt;

    public TransportPayoutListDTO() {
    }

    public Long getPayoutId() {
        return payoutId;
    }

    public void setPayoutId(Long payoutId) {
        this.payoutId = payoutId;
    }

    public String getPayoutNumber() {
        return payoutNumber;
    }

    public void setPayoutNumber(String payoutNumber) {
        this.payoutNumber = payoutNumber;
    }

    public Long getTotalAmountVnd() {
        return totalAmountVnd;
    }

    public void setTotalAmountVnd(Long totalAmountVnd) {
        this.totalAmountVnd = totalAmountVnd;
    }

    public Integer getItemCount() {
        return itemCount;
    }

    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
    }

    public PayoutStatus getStatus() {
        return status;
    }

    public void setStatus(PayoutStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
