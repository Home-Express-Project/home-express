package com.homeexpress.home_express_api.dto.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentSummaryDTO {

    private Long bookingId;
    private Long customerId;
    private Long transportId;
    private BigDecimal bookingAmountVnd;
    private BigDecimal totalPaidVnd;
    private BigDecimal depositPaidVnd;
    private BigDecimal balancePaidVnd;
    private BigDecimal totalRefundedVnd;
    private LocalDateTime lastPaidAt;
    private Integer paymentCount;
    private BigDecimal outstandingVnd;

    public PaymentSummaryDTO() {
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getTransportId() {
        return transportId;
    }

    public void setTransportId(Long transportId) {
        this.transportId = transportId;
    }

    public BigDecimal getBookingAmountVnd() {
        return bookingAmountVnd;
    }

    public void setBookingAmountVnd(BigDecimal bookingAmountVnd) {
        this.bookingAmountVnd = bookingAmountVnd;
    }

    public BigDecimal getTotalPaidVnd() {
        return totalPaidVnd;
    }

    public void setTotalPaidVnd(BigDecimal totalPaidVnd) {
        this.totalPaidVnd = totalPaidVnd;
    }

    public BigDecimal getDepositPaidVnd() {
        return depositPaidVnd;
    }

    public void setDepositPaidVnd(BigDecimal depositPaidVnd) {
        this.depositPaidVnd = depositPaidVnd;
    }

    public BigDecimal getBalancePaidVnd() {
        return balancePaidVnd;
    }

    public void setBalancePaidVnd(BigDecimal balancePaidVnd) {
        this.balancePaidVnd = balancePaidVnd;
    }

    public BigDecimal getTotalRefundedVnd() {
        return totalRefundedVnd;
    }

    public void setTotalRefundedVnd(BigDecimal totalRefundedVnd) {
        this.totalRefundedVnd = totalRefundedVnd;
    }

    public LocalDateTime getLastPaidAt() {
        return lastPaidAt;
    }

    public void setLastPaidAt(LocalDateTime lastPaidAt) {
        this.lastPaidAt = lastPaidAt;
    }

    public Integer getPaymentCount() {
        return paymentCount;
    }

    public void setPaymentCount(Integer paymentCount) {
        this.paymentCount = paymentCount;
    }

    public BigDecimal getOutstandingVnd() {
        return outstandingVnd;
    }

    public void setOutstandingVnd(BigDecimal outstandingVnd) {
        this.outstandingVnd = outstandingVnd;
    }
}
