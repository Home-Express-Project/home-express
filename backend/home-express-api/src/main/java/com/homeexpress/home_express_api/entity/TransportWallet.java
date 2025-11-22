package com.homeexpress.home_express_api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transport_wallets")
public class TransportWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_id")
    private Long walletId;

    @Column(name = "transport_id", nullable = false, unique = true)
    private Long transportId;

    @Column(name = "current_balance_vnd", nullable = false)
    private Long currentBalanceVnd = 0L;

    @Column(name = "total_earned_vnd", nullable = false)
    private Long totalEarnedVnd = 0L;

    @Column(name = "total_withdrawn_vnd", nullable = false)
    private Long totalWithdrawnVnd = 0L;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransportWalletStatus status = TransportWalletStatus.ACTIVE;

    @Column(name = "last_transaction_at")
    private LocalDateTime lastTransactionAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getWalletId() {
        return walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public Long getTransportId() {
        return transportId;
    }

    public void setTransportId(Long transportId) {
        this.transportId = transportId;
    }

    public Long getCurrentBalanceVnd() {
        return currentBalanceVnd;
    }

    public void setCurrentBalanceVnd(Long currentBalanceVnd) {
        this.currentBalanceVnd = currentBalanceVnd;
    }

    public Long getTotalEarnedVnd() {
        return totalEarnedVnd;
    }

    public void setTotalEarnedVnd(Long totalEarnedVnd) {
        this.totalEarnedVnd = totalEarnedVnd;
    }

    public Long getTotalWithdrawnVnd() {
        return totalWithdrawnVnd;
    }

    public void setTotalWithdrawnVnd(Long totalWithdrawnVnd) {
        this.totalWithdrawnVnd = totalWithdrawnVnd;
    }

    public TransportWalletStatus getStatus() {
        return status;
    }

    public void setStatus(TransportWalletStatus status) {
        this.status = status;
    }

    public LocalDateTime getLastTransactionAt() {
        return lastTransactionAt;
    }

    public void setLastTransactionAt(LocalDateTime lastTransactionAt) {
        this.lastTransactionAt = lastTransactionAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
