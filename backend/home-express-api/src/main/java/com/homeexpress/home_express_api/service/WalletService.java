package com.homeexpress.home_express_api.service;

import com.homeexpress.home_express_api.entity.TransportWallet;
import com.homeexpress.home_express_api.entity.TransportWalletStatus;
import com.homeexpress.home_express_api.entity.TransportWalletTransaction;
import com.homeexpress.home_express_api.entity.WalletTransactionReferenceType;
import com.homeexpress.home_express_api.entity.WalletTransactionType;
import com.homeexpress.home_express_api.exception.ResourceNotFoundException;
import com.homeexpress.home_express_api.repository.TransportWalletRepository;
import com.homeexpress.home_express_api.repository.TransportWalletTransactionRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {

    private static final Logger log = LoggerFactory.getLogger(WalletService.class);

    private final TransportWalletRepository walletRepository;
    private final TransportWalletTransactionRepository transactionRepository;

    public WalletService(TransportWalletRepository walletRepository,
            TransportWalletTransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Ensure a transport has an initialized wallet.
     */
    @Transactional
    public TransportWallet getOrCreateWallet(Long transportId) {
        return walletRepository.findByTransportId(transportId)
                .orElseGet(() -> {
                    TransportWallet wallet = new TransportWallet();
                    wallet.setTransportId(transportId);
                    wallet.setCurrentBalanceVnd(0L);
                    wallet.setTotalEarnedVnd(0L);
                    wallet.setTotalWithdrawnVnd(0L);
                    wallet.setStatus(TransportWalletStatus.ACTIVE);
                    return walletRepository.save(wallet);
                });
    }

    /**
     * Credit wallet (e.g., settlement completion).
     */
    @Transactional
    public TransportWalletTransaction creditWallet(Long walletId, Long amount,
            WalletTransactionType transactionType,
            WalletTransactionReferenceType referenceType,
            Long referenceId,
            String description,
            Long actorId) {
        TransportWallet wallet = loadWallet(walletId);
        validateAmount(amount);

        if (wallet.getStatus() == TransportWalletStatus.FROZEN
                || wallet.getStatus() == TransportWalletStatus.SUSPENDED) {
            throw new IllegalStateException("Wallet is not allowed to receive credits in its current status");
        }

        WalletTransactionType resolvedType = transactionType != null
                ? transactionType
                : WalletTransactionType.SETTLEMENT_CREDIT;

        long newBalance = wallet.getCurrentBalanceVnd() + amount;

        TransportWalletTransaction transaction = buildTransaction(wallet, resolvedType, amount, newBalance,
                referenceType, referenceId, description, actorId);

        wallet.setCurrentBalanceVnd(newBalance);
        wallet.setTotalEarnedVnd(wallet.getTotalEarnedVnd() + amount);
        wallet.setLastTransactionAt(transaction.getCreatedAt());

        walletRepository.save(wallet);
        return transactionRepository.save(transaction);
    }

    /**
     * Debit wallet (e.g., payout initiation).
     */
    @Transactional
    public TransportWalletTransaction debitWallet(Long walletId, Long amount,
            WalletTransactionType transactionType,
            WalletTransactionReferenceType referenceType,
            Long referenceId,
            String description,
            Long actorId) {
        TransportWallet wallet = loadWallet(walletId);
        validateAmount(amount);

        if (wallet.getStatus() != TransportWalletStatus.ACTIVE) {
            throw new IllegalStateException("Wallet must be ACTIVE to perform debit operations");
        }

        if (wallet.getCurrentBalanceVnd() < amount) {
            throw new IllegalStateException("Insufficient wallet balance for debit");
        }

        WalletTransactionType resolvedType = transactionType != null
                ? transactionType
                : WalletTransactionType.PAYOUT_DEBIT;

        long newBalance = wallet.getCurrentBalanceVnd() - amount;

        TransportWalletTransaction transaction = buildTransaction(wallet, resolvedType, amount, newBalance,
                referenceType, referenceId, description, actorId);

        wallet.setCurrentBalanceVnd(newBalance);
        wallet.setTotalWithdrawnVnd(wallet.getTotalWithdrawnVnd() + amount);
        wallet.setLastTransactionAt(transaction.getCreatedAt());

        walletRepository.save(wallet);
        return transactionRepository.save(transaction);
    }

    /**
     * Apply an adjustment credit while keeping a full ledger trail.
     */
    @Transactional
    public TransportWalletTransaction applyAdjustmentCredit(Long transportId,
            Long amount,
            WalletTransactionReferenceType referenceType,
            Long referenceId,
            String description,
            Long actorId) {
        TransportWallet wallet = getOrCreateWallet(transportId);
        WalletTransactionReferenceType resolvedReference = referenceType != null
                ? referenceType
                : WalletTransactionReferenceType.ADJUSTMENT;
        return creditWallet(wallet.getWalletId(), amount,
                WalletTransactionType.ADJUSTMENT_CREDIT,
                resolvedReference,
                referenceId,
                description,
                actorId);
    }

    /**
     * Apply an adjustment debit while keeping a full ledger trail.
     */
    @Transactional
    public TransportWalletTransaction applyAdjustmentDebit(Long transportId,
            Long amount,
            WalletTransactionReferenceType referenceType,
            Long referenceId,
            String description,
            Long actorId) {
        TransportWallet wallet = getOrCreateWallet(transportId);
        WalletTransactionReferenceType resolvedReference = referenceType != null
                ? referenceType
                : WalletTransactionReferenceType.ADJUSTMENT;
        return debitWallet(wallet.getWalletId(), amount,
                WalletTransactionType.ADJUSTMENT_DEBIT,
                resolvedReference,
                referenceId,
                description,
                actorId);
    }

    @Transactional
    public void freezeWallet(Long walletId) {
        TransportWallet wallet = loadWallet(walletId);
        wallet.setStatus(TransportWalletStatus.FROZEN);
        walletRepository.save(wallet);
        log.info("Wallet {} frozen.", walletId);
    }

    @Transactional
    public void unfreezeWallet(Long walletId) {
        TransportWallet wallet = loadWallet(walletId);
        wallet.setStatus(TransportWalletStatus.ACTIVE);
        walletRepository.save(wallet);
        log.info("Wallet {} unfrozen.", walletId);
    }

    @Transactional
    public void suspendWallet(Long walletId) {
        TransportWallet wallet = loadWallet(walletId);
        wallet.setStatus(TransportWalletStatus.SUSPENDED);
        walletRepository.save(wallet);
        log.info("Wallet {} suspended.", walletId);
    }

    /**
     * Calculate balance from ledger for integrity checks.
     */
    public long calculateBalance(Long walletId) {
        return recalculateBalanceFromLedger(walletId).getLedgerBalanceVnd();
    }

    /**
     * Replays the ledger to verify running balances stay consistent with wallet totals.
     */
    public LedgerRecalculationResult recalculateBalanceFromLedger(Long walletId) {
        TransportWallet wallet = loadWallet(walletId);
        List<TransportWalletTransaction> transactions =
                transactionRepository.findByWallet_WalletIdOrderByCreatedAtAsc(walletId);

        long runningBalance = 0L;
        int mismatchedTransactions = 0;
        Long lastTransactionBalance = null;

        for (TransportWalletTransaction transaction : transactions) {
            long signedAmount = resolveSignedAmount(transaction);
            runningBalance += signedAmount;

            lastTransactionBalance = transaction.getRunningBalanceVnd();
            if (lastTransactionBalance == null || lastTransactionBalance.longValue() != runningBalance) {
                mismatchedTransactions++;
            }
        }

        LedgerRecalculationResult result = new LedgerRecalculationResult(
                walletId,
                runningBalance,
                wallet.getCurrentBalanceVnd(),
                lastTransactionBalance,
                mismatchedTransactions);

        if (!result.isConsistent()) {
            log.warn("Wallet {} ledger mismatch detected. stored={}, ledger={}, last_tx_balance={}, mismatched_tx={}",
                    walletId,
                    result.getStoredBalanceVnd(),
                    result.getLedgerBalanceVnd(),
                    result.getLastTransactionBalanceVnd(),
                    result.getMismatchedTransactions());
        }

        return result;
    }

    public List<TransportWalletTransaction> getTransactionHistory(Long walletId, LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null) {
            return transactionRepository.findByWallet_WalletIdAndCreatedAtBetweenOrderByCreatedAtDesc(walletId, from, to);
        }
        return transactionRepository.findByWallet_WalletIdOrderByCreatedAtDesc(walletId);
    }

    public boolean hasReferenceTransaction(WalletTransactionReferenceType referenceType, Long referenceId,
            WalletTransactionType transactionType) {
        return transactionRepository.existsByReferenceTypeAndReferenceIdAndTransactionType(
                referenceType, referenceId, transactionType);
    }

    public boolean hasSettlementCredit(Long settlementId) {
        return hasReferenceTransaction(WalletTransactionReferenceType.SETTLEMENT, settlementId,
                WalletTransactionType.SETTLEMENT_CREDIT);
    }

    public void reversePayoutIfNeeded(Long transportId, Long payoutId, Long amount) {
        if (amount == null || amount <= 0) {
            return;
        }

        TransportWallet wallet = getOrCreateWallet(transportId);
        boolean debitExists = hasReferenceTransaction(
                WalletTransactionReferenceType.PAYOUT, payoutId, WalletTransactionType.PAYOUT_DEBIT);

        boolean reversalExists = hasReferenceTransaction(
                WalletTransactionReferenceType.PAYOUT, payoutId, WalletTransactionType.REVERSAL);

        if (debitExists && !reversalExists) {
            creditWallet(wallet.getWalletId(), amount, WalletTransactionType.REVERSAL,
                    WalletTransactionReferenceType.PAYOUT, payoutId,
                    "Reversal for failed payout " + payoutId, null);
        }
    }

    private TransportWallet loadWallet(Long walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("TransportWallet", "walletId", walletId));
    }

    private void validateAmount(Long amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }

    private TransportWalletTransaction buildTransaction(TransportWallet wallet,
            WalletTransactionType transactionType,
            Long amount,
            Long runningBalance,
            WalletTransactionReferenceType referenceType,
            Long referenceId,
            String description,
            Long actorId) {
        TransportWalletTransaction transaction = new TransportWalletTransaction();
        LocalDateTime now = LocalDateTime.now();
        transaction.setWallet(wallet);
        transaction.setTransactionType(transactionType);
        transaction.setAmount(amount);
        transaction.setRunningBalanceVnd(runningBalance);
        transaction.setReferenceType(referenceType);
        transaction.setReferenceId(referenceId);
        transaction.setDescription(description);
        transaction.setCreatedBy(actorId);
        transaction.setCreatedAt(now);
        return transaction;
    }

    private long resolveSignedAmount(TransportWalletTransaction transaction) {
        long amount = transaction.getAmount() != null ? transaction.getAmount() : 0L;
        WalletTransactionType type = transaction.getTransactionType();
        if (type == null) {
            return amount;
        }
        return switch (type) {
            case PAYOUT_DEBIT, ADJUSTMENT_DEBIT -> -amount;
            default -> amount;
        };
    }

    public static class LedgerRecalculationResult {

        private final Long walletId;
        private final long ledgerBalanceVnd;
        private final long storedBalanceVnd;
        private final Long lastTransactionBalanceVnd;
        private final int mismatchedTransactions;

        public LedgerRecalculationResult(Long walletId,
                long ledgerBalanceVnd,
                long storedBalanceVnd,
                Long lastTransactionBalanceVnd,
                int mismatchedTransactions) {
            this.walletId = walletId;
            this.ledgerBalanceVnd = ledgerBalanceVnd;
            this.storedBalanceVnd = storedBalanceVnd;
            this.lastTransactionBalanceVnd = lastTransactionBalanceVnd;
            this.mismatchedTransactions = mismatchedTransactions;
        }

        public Long getWalletId() {
            return walletId;
        }

        public long getLedgerBalanceVnd() {
            return ledgerBalanceVnd;
        }

        public long getStoredBalanceVnd() {
            return storedBalanceVnd;
        }

        public Long getLastTransactionBalanceVnd() {
            return lastTransactionBalanceVnd;
        }

        public int getMismatchedTransactions() {
            return mismatchedTransactions;
        }

        public boolean isConsistent() {
            boolean runningMatches = mismatchedTransactions == 0
                    && (lastTransactionBalanceVnd == null || lastTransactionBalanceVnd.longValue() == ledgerBalanceVnd);
            boolean walletMatches = storedBalanceVnd == ledgerBalanceVnd;
            return runningMatches && walletMatches;
        }
    }
}
