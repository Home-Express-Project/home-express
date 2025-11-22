package com.homeexpress.home_express_api.repository;

import com.homeexpress.home_express_api.entity.TransportPayoutItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for TransportPayoutItem entity.
 * Provides data access methods for managing payout items.
 */
@Repository
public interface TransportPayoutItemRepository extends JpaRepository<TransportPayoutItem, Long> {

    /**
     * Find all items by payout ID.
     *
     * @param payoutId the payout ID
     * @return list of payout items
     */
    List<TransportPayoutItem> findByPayoutId(Long payoutId);

    /**
     * Find payout item by settlement ID.
     *
     * @param settlementId the settlement ID
     * @return list of payout items (should be 0 or 1)
     */
    List<TransportPayoutItem> findBySettlementId(Long settlementId);

    /**
     * Delete all items by payout ID.
     *
     * @param payoutId the payout ID
     */
    void deleteByPayoutId(Long payoutId);
}
