package com.homeexpress.home_express_api.repository;

import com.homeexpress.home_express_api.entity.TransportWalletTransaction;
import com.homeexpress.home_express_api.entity.WalletTransactionReferenceType;
import com.homeexpress.home_express_api.entity.WalletTransactionType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransportWalletTransactionRepository extends JpaRepository<TransportWalletTransaction, Long> {

    List<TransportWalletTransaction> findByWallet_WalletIdOrderByCreatedAtAsc(Long walletId);

    List<TransportWalletTransaction> findByWallet_WalletIdOrderByCreatedAtDesc(Long walletId);

    List<TransportWalletTransaction> findByWallet_WalletIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long walletId, LocalDateTime start, LocalDateTime end);

    List<TransportWalletTransaction> findByWallet_WalletIdAndCreatedAtBetweenOrderByCreatedAtAsc(
            Long walletId, LocalDateTime start, LocalDateTime end);

    List<TransportWalletTransaction> findByWallet_WalletIdAndReferenceTypeOrderByCreatedAtAsc(
            Long walletId, WalletTransactionReferenceType referenceType);

    Optional<TransportWalletTransaction> findTopByWallet_WalletIdAndCreatedAtBeforeOrderByCreatedAtDesc(
            Long walletId, LocalDateTime createdBefore);

    boolean existsByReferenceTypeAndReferenceIdAndTransactionType(
            WalletTransactionReferenceType referenceType,
            Long referenceId,
            WalletTransactionType transactionType);

    List<TransportWalletTransaction> findByReferenceTypeAndReferenceId(
            WalletTransactionReferenceType referenceType,
            Long referenceId);
}
