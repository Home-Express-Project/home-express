package com.homeexpress.home_express_api.repository;

import com.homeexpress.home_express_api.entity.PayoutStatus;
import com.homeexpress.home_express_api.entity.TransportPayout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for TransportPayout entity.
 * Provides data access methods for managing transport payouts.
 */
@Repository
public interface TransportPayoutRepository extends JpaRepository<TransportPayout, Long> {

    /**
     * Find all payouts by transport ID.
     *
     * @param transportId the transport company ID
     * @return list of payouts for the transport
     */
    List<TransportPayout> findByTransportId(Long transportId);

    /**
     * Find payouts by status.
     *
     * @param status the payout status
     * @return list of payouts with the given status
     */
    List<TransportPayout> findByStatus(PayoutStatus status);

    /**
     * Find payouts by transport ID ordered by creation date descending.
     *
     * @param transportId the transport company ID
     * @return list of payouts ordered by newest first
     */
    List<TransportPayout> findByTransportIdOrderByCreatedAtDesc(Long transportId);

    /**
     * Find payout by payout number.
     *
     * @param payoutNumber the unique payout number
     * @return optional payout
     */
    Optional<TransportPayout> findByPayoutNumber(String payoutNumber);

    /**
     * Check if a payout number already exists.
     *
     * @param payoutNumber the payout number to check
     * @return true if exists, false otherwise
     */
    boolean existsByPayoutNumber(String payoutNumber);
}
