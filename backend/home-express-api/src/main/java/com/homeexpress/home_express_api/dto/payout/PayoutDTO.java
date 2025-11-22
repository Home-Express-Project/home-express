package com.homeexpress.home_express_api.dto.payout;

import com.homeexpress.home_express_api.entity.PayoutStatus;
import com.homeexpress.home_express_api.entity.TransportPayout;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for TransportPayout entity.
 * Used to transfer payout data between layers.
 */
public class PayoutDTO {

    private Long payoutId;
    private Long transportId;
    private String payoutNumber;
    private Long totalAmountVnd;
    private Integer itemCount;
    private PayoutStatus status;
    private String bankCode;
    private String bankAccountNumber;
    private String bankAccountHolder;
    private LocalDateTime processedAt;
    private LocalDateTime completedAt;
    private String failureReason;
    private String transactionReference;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PayoutItemDTO> items;

    public PayoutDTO() {
    }

    public static PayoutDTO fromEntity(TransportPayout payout) {
        PayoutDTO dto = new PayoutDTO();
        dto.setPayoutId(payout.getPayoutId());
        dto.setTransportId(payout.getTransportId());
        dto.setPayoutNumber(payout.getPayoutNumber());
        dto.setTotalAmountVnd(payout.getTotalAmountVnd());
        dto.setItemCount(payout.getItemCount());
        dto.setStatus(payout.getStatus());
        dto.setBankCode(payout.getBankCode());
        dto.setBankAccountNumber(payout.getBankAccountNumber());
        dto.setBankAccountHolder(payout.getBankAccountHolder());
        dto.setProcessedAt(payout.getProcessedAt());
        dto.setCompletedAt(payout.getCompletedAt());
        dto.setFailureReason(payout.getFailureReason());
        dto.setTransactionReference(payout.getTransactionReference());
        dto.setNotes(payout.getNotes());
        dto.setCreatedAt(payout.getCreatedAt());
        dto.setUpdatedAt(payout.getUpdatedAt());
        dto.setItems(new ArrayList<>());
        return dto;
    }

    public Long getPayoutId() {
        return payoutId;
    }

    public void setPayoutId(Long payoutId) {
        this.payoutId = payoutId;
    }

    public Long getTransportId() {
        return transportId;
    }

    public void setTransportId(Long transportId) {
        this.transportId = transportId;
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

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public String getBankAccountHolder() {
        return bankAccountHolder;
    }

    public void setBankAccountHolder(String bankAccountHolder) {
        this.bankAccountHolder = bankAccountHolder;
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

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<PayoutItemDTO> getItems() {
        return items;
    }

    public void setItems(List<PayoutItemDTO> items) {
        this.items = items;
    }
}
