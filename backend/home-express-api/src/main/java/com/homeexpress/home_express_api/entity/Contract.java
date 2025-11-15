package com.homeexpress.home_express_api.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "contracts")
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contract_id")
    private Long contractId;

    @NotNull
    @Column(name = "booking_id", nullable = false, unique = true)
    private Long bookingId;

    @NotNull
    @Column(name = "quotation_id", nullable = false)
    private Long quotationId;

    @NotNull
    @Column(name = "contract_number", nullable = false, unique = true, length = 50)
    private String contractNumber;

    @NotNull
    @Column(name = "terms_and_conditions", nullable = false, columnDefinition = "TEXT")
    private String termsAndConditions;

    @NotNull
    @Positive
    @Column(name = "total_amount", nullable = false, precision = 12)
    private BigDecimal totalAmount;

    @NotNull
    @Positive
    @Column(name = "agreed_price_vnd", nullable = false)
    private Long agreedPriceVnd = 0L;

    @NotNull
    @Column(name = "deposit_required_vnd", nullable = false)
    private Long depositRequiredVnd = 0L;

    @Column(name = "deposit_due_at")
    private LocalDateTime depositDueAt;

    @Column(name = "balance_due_at")
    private LocalDateTime balanceDueAt;

    @Column(name = "customer_signed")
    private Boolean customerSigned = false;

    @Column(name = "customer_signed_at")
    private LocalDateTime customerSignedAt;

    @Column(name = "customer_signature_url", columnDefinition = "TEXT")
    private String customerSignatureUrl;

    @Column(name = "customer_signed_ip", length = 45)
    private String customerSignedIp;

    @Column(name = "transport_signed")
    private Boolean transportSigned = false;

    @Column(name = "transport_signed_at")
    private LocalDateTime transportSignedAt;

    @Column(name = "transport_signature_url", columnDefinition = "TEXT")
    private String transportSignatureUrl;

    @Column(name = "transport_signed_ip", length = 45)
    private String transportSignedIp;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ContractStatus status = ContractStatus.DRAFT;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    private LocalDateTime updatedAt;

    public Contract() {
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

    public String getCustomerSignedIp() {
        return customerSignedIp;
    }

    public void setCustomerSignedIp(String customerSignedIp) {
        this.customerSignedIp = customerSignedIp;
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

    public String getTransportSignedIp() {
        return transportSignedIp;
    }

    public void setTransportSignedIp(String transportSignedIp) {
        this.transportSignedIp = transportSignedIp;
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
