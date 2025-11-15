package com.homeexpress.home_express_api.dto.transport;

public class TransportSettlementSummaryDTO {

    private Long totalPendingAmountVnd;
    private Integer pendingCount;
    private Long totalReadyAmountVnd;
    private Integer readyCount;
    private Long totalInPayoutAmountVnd;
    private Integer inPayoutCount;
    private Long totalPaidAmountVnd;
    private Integer paidCount;
    private Long totalOnHoldAmountVnd;
    private Integer onHoldCount;

    public TransportSettlementSummaryDTO() {
    }

    public Long getTotalPendingAmountVnd() {
        return totalPendingAmountVnd;
    }

    public void setTotalPendingAmountVnd(Long totalPendingAmountVnd) {
        this.totalPendingAmountVnd = totalPendingAmountVnd;
    }

    public Integer getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(Integer pendingCount) {
        this.pendingCount = pendingCount;
    }

    public Long getTotalReadyAmountVnd() {
        return totalReadyAmountVnd;
    }

    public void setTotalReadyAmountVnd(Long totalReadyAmountVnd) {
        this.totalReadyAmountVnd = totalReadyAmountVnd;
    }

    public Integer getReadyCount() {
        return readyCount;
    }

    public void setReadyCount(Integer readyCount) {
        this.readyCount = readyCount;
    }

    public Long getTotalInPayoutAmountVnd() {
        return totalInPayoutAmountVnd;
    }

    public void setTotalInPayoutAmountVnd(Long totalInPayoutAmountVnd) {
        this.totalInPayoutAmountVnd = totalInPayoutAmountVnd;
    }

    public Integer getInPayoutCount() {
        return inPayoutCount;
    }

    public void setInPayoutCount(Integer inPayoutCount) {
        this.inPayoutCount = inPayoutCount;
    }

    public Long getTotalPaidAmountVnd() {
        return totalPaidAmountVnd;
    }

    public void setTotalPaidAmountVnd(Long totalPaidAmountVnd) {
        this.totalPaidAmountVnd = totalPaidAmountVnd;
    }

    public Integer getPaidCount() {
        return paidCount;
    }

    public void setPaidCount(Integer paidCount) {
        this.paidCount = paidCount;
    }

    public Long getTotalOnHoldAmountVnd() {
        return totalOnHoldAmountVnd;
    }

    public void setTotalOnHoldAmountVnd(Long totalOnHoldAmountVnd) {
        this.totalOnHoldAmountVnd = totalOnHoldAmountVnd;
    }

    public Integer getOnHoldCount() {
        return onHoldCount;
    }

    public void setOnHoldCount(Integer onHoldCount) {
        this.onHoldCount = onHoldCount;
    }
}
