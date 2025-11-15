package com.homeexpress.home_express_api.dto.transport;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.homeexpress.home_express_api.entity.WalletTransactionReferenceType;
import com.homeexpress.home_express_api.entity.WalletTransactionType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TransportWalletReportResponse {

    private WalletSnapshot snapshot = new WalletSnapshot();
    private List<CashflowEntry> cashflow = new ArrayList<>();
    private List<DailyBalancePoint> dailyBalances = new ArrayList<>();
    private ReconciliationReport reconciliation = new ReconciliationReport();

    public WalletSnapshot getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(WalletSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    public List<CashflowEntry> getCashflow() {
        return cashflow;
    }

    public void setCashflow(List<CashflowEntry> cashflow) {
        this.cashflow = cashflow;
    }

    public List<DailyBalancePoint> getDailyBalances() {
        return dailyBalances;
    }

    public void setDailyBalances(List<DailyBalancePoint> dailyBalances) {
        this.dailyBalances = dailyBalances;
    }

    public ReconciliationReport getReconciliation() {
        return reconciliation;
    }

    public void setReconciliation(ReconciliationReport reconciliation) {
        this.reconciliation = reconciliation;
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class WalletSnapshot {
        private Long currentBalanceVnd = 0L;
        private Long totalEarnedVnd = 0L;
        private Long totalWithdrawnVnd = 0L;
        private LocalDateTime lastTransactionAt;

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

        public LocalDateTime getLastTransactionAt() {
            return lastTransactionAt;
        }

        public void setLastTransactionAt(LocalDateTime lastTransactionAt) {
            this.lastTransactionAt = lastTransactionAt;
        }
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class CashflowEntry {
        private Long transactionId;
        private WalletTransactionType transactionType;
        private WalletTransactionReferenceType referenceType;
        private Long referenceId;
        private Long bookingId;
        private Long payoutId;
        private Long amountVnd;
        private Long runningBalanceVnd;
        private boolean inflow;
        private LocalDateTime createdAt;

        public Long getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(Long transactionId) {
            this.transactionId = transactionId;
        }

        public WalletTransactionType getTransactionType() {
            return transactionType;
        }

        public void setTransactionType(WalletTransactionType transactionType) {
            this.transactionType = transactionType;
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

        public Long getBookingId() {
            return bookingId;
        }

        public void setBookingId(Long bookingId) {
            this.bookingId = bookingId;
        }

        public Long getPayoutId() {
            return payoutId;
        }

        public void setPayoutId(Long payoutId) {
            this.payoutId = payoutId;
        }

        public Long getAmountVnd() {
            return amountVnd;
        }

        public void setAmountVnd(Long amountVnd) {
            this.amountVnd = amountVnd;
        }

        public Long getRunningBalanceVnd() {
            return runningBalanceVnd;
        }

        public void setRunningBalanceVnd(Long runningBalanceVnd) {
            this.runningBalanceVnd = runningBalanceVnd;
        }

        public boolean isInflow() {
            return inflow;
        }

        public void setInflow(boolean inflow) {
            this.inflow = inflow;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class DailyBalancePoint {
        private String date;
        private Long closingBalanceVnd;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public Long getClosingBalanceVnd() {
            return closingBalanceVnd;
        }

        public void setClosingBalanceVnd(Long closingBalanceVnd) {
            this.closingBalanceVnd = closingBalanceVnd;
        }
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ReconciliationReport {
        private Integer settlementCount = 0;
        private Integer walletSettlementCount = 0;
        private List<Long> missingSettlementIds = new ArrayList<>();
        private Integer payoutCount = 0;
        private Integer walletPayoutCount = 0;
        private List<Long> missingPayoutIds = new ArrayList<>();
        private boolean balanced = true;

        public Integer getSettlementCount() {
            return settlementCount;
        }

        public void setSettlementCount(Integer settlementCount) {
            this.settlementCount = settlementCount;
        }

        public Integer getWalletSettlementCount() {
            return walletSettlementCount;
        }

        public void setWalletSettlementCount(Integer walletSettlementCount) {
            this.walletSettlementCount = walletSettlementCount;
        }

        public List<Long> getMissingSettlementIds() {
            return missingSettlementIds;
        }

        public void setMissingSettlementIds(List<Long> missingSettlementIds) {
            this.missingSettlementIds = missingSettlementIds;
        }

        public Integer getPayoutCount() {
            return payoutCount;
        }

        public void setPayoutCount(Integer payoutCount) {
            this.payoutCount = payoutCount;
        }

        public Integer getWalletPayoutCount() {
            return walletPayoutCount;
        }

        public void setWalletPayoutCount(Integer walletPayoutCount) {
            this.walletPayoutCount = walletPayoutCount;
        }

        public List<Long> getMissingPayoutIds() {
            return missingPayoutIds;
        }

        public void setMissingPayoutIds(List<Long> missingPayoutIds) {
            this.missingPayoutIds = missingPayoutIds;
        }

        public boolean isBalanced() {
            return balanced;
        }

        public void setBalanced(boolean balanced) {
            this.balanced = balanced;
        }
    }
}
