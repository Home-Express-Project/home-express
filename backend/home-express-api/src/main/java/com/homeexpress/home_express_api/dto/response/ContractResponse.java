package com.homeexpress.home_express_api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.homeexpress.home_express_api.entity.ContractStatus;

public class ContractResponse {

    private Long contractId;
    private Long bookingId;
    private Long quotationId;
    private String contractNumber;
    private String termsAndConditions;
    private BigDecimal totalAmount;
    private Long agreedPriceVnd;
    private Long depositRequiredVnd;
    private LocalDateTime depositDueAt;
    private LocalDateTime balanceDueAt;
    private Boolean customerSigned;
    private LocalDateTime customerSignedAt;
    private String customerSignatureUrl;
    private Boolean transportSigned;
    private LocalDateTime transportSignedAt;
    private String transportSignatureUrl;
    private ContractStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ContractResponse() {
    }

    public Long getContractId() {
        return contractId;
    }

    public void setContractId(Long contractId) {
        this.contractId = contractId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getQuotationId() {
        return quotationId;
    }

    public void setQuotationId(Long quotationId) {
        this.quotationId = quotationId;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    public String getTermsAndConditions() {
        return termsAndConditions;
    }

    public void setTermsAndConditions(String termsAndConditions) {
        this.termsAndConditions = termsAndConditions;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Long getAgreedPriceVnd() {
        return agreedPriceVnd;
    }

    public void setAgreedPriceVnd(Long agreedPriceVnd) {
        this.agreedPriceVnd = agreedPriceVnd;
    }

    public Long getDepositRequiredVnd() {
        return depositRequiredVnd;
    }

    public void setDepositRequiredVnd(Long depositRequiredVnd) {
        this.depositRequiredVnd = depositRequiredVnd;
    }

    public LocalDateTime getDepositDueAt() {
        return depositDueAt;
    }

    public void setDepositDueAt(LocalDateTime depositDueAt) {
        this.depositDueAt = depositDueAt;
    }

    public LocalDateTime getBalanceDueAt() {
        return balanceDueAt;
    }

    public void setBalanceDueAt(LocalDateTime balanceDueAt) {
        this.balanceDueAt = balanceDueAt;
    }

    public Boolean getCustomerSigned() {
        return customerSigned;
    }

    public void setCustomerSigned(Boolean customerSigned) {
        this.customerSigned = customerSigned;
    }

    public LocalDateTime getCustomerSignedAt() {
        return customerSignedAt;
    }

    public void setCustomerSignedAt(LocalDateTime customerSignedAt) {
        this.customerSignedAt = customerSignedAt;
    }

    public String getCustomerSignatureUrl() {
        return customerSignatureUrl;
    }

    public void setCustomerSignatureUrl(String customerSignatureUrl) {
        this.customerSignatureUrl = customerSignatureUrl;
    }

    public Boolean getTransportSigned() {
        return transportSigned;
    }

    public void setTransportSigned(Boolean transportSigned) {
        this.transportSigned = transportSigned;
    }

    public LocalDateTime getTransportSignedAt() {
        return transportSignedAt;
    }

    public void setTransportSignedAt(LocalDateTime transportSignedAt) {
        this.transportSignedAt = transportSignedAt;
    }

    public String getTransportSignatureUrl() {
        return transportSignatureUrl;
    }

    public void setTransportSignatureUrl(String transportSignatureUrl) {
        this.transportSignatureUrl = transportSignatureUrl;
    }

    public ContractStatus getStatus() {
        return status;
    }

    public void setStatus(ContractStatus status) {
        this.status = status;
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
}
