package com.homeexpress.home_express_api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transport_wallet_transactions")
public class TransportWalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private TransportWallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    private WalletTransactionType transactionType;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "running_balance_vnd", nullable = false)
    private Long runningBalanceVnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", length = 30)
    private WalletTransactionReferenceType referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public TransportWallet getWallet() {
        return wallet;
    }

    public void setWallet(TransportWallet wallet) {
        this.wallet = wallet;
    }

    public WalletTransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(WalletTransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Long getRunningBalanceVnd() {
        return runningBalanceVnd;
    }

    public void setRunningBalanceVnd(Long runningBalanceVnd) {
        this.runningBalanceVnd = runningBalanceVnd;
    }

    public WalletTransactionReferenceType getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(WalletTransactionReferenceType referenceType) {
        this.referenceType = referenceType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
}
